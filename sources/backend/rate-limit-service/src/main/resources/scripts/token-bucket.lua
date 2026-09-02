lua
-- =============================================================================
-- Enterprise Master Governance Guardrails & Traceability Compliance
-- =============================================================================
-- Target Project Identity: social-scheduler
-- Target Component Destination Path: ./sources/backend/rate-limit-service/src/main/resources/scripts/token-bucket.lua
-- Traceability Audit Tags: [REQ-003], [EXC-005]
-- =============================================================================
-- Business Context & Technical Architecture:
-- This Lua script implements a high-performance, atomic Token Bucket rate-limiting
-- algorithm directly inside Redis. It guarantees thread-safe execution across
-- distributed microservice instances by leveraging Redis EVAL commands.
-- 
-- Algorithm Flow:
-- 1. KEYS[1]: Redis key representing the rate limit bucket (e.g., rate_limit:{userId}:{endpoint})
-- 2. ARGV[1]: Current timestamp in milliseconds (supplied by Redis server time or application)
-- 3. ARGV[2]: Maximum capacity of the token bucket (e.g., 100)
-- 4. ARGV[3]: Refill rate in tokens per minute (e.g., 60)
-- 5. ARGV[4]: Number of tokens requested for the current transaction (e.g., 1)
-- 
-- Return Structure (Redis Multi-Bulk Reply / Lua Table converted to array):
-- - Index 1: 1 if allowed, 0 if rate limit exceeded (rejected)
-- - Index 2: Remaining tokens in the bucket after consumption (or 0 if rejected)
-- - Index 3: Retry-after duration in seconds (0 if allowed, calculated delay if rejected)
-- =============================================================================

-- [REQ-003] Extract input arguments with explicit type coercion to ensure robust arithmetic
local rateLimitKey = KEYS[1]
local currentTimeMs = tonumber(ARGV[1])
local maxCapacity = tonumber(ARGV[2])
local refillRatePerMinute = tonumber(ARGV[3])
local requestedTokens = tonumber(ARGV[4])

-- [REQ-003] Calculate token refill parameters per millisecond for high-precision sliding window
local refillRatePerMs = refillRatePerMinute / 60000.0

-- [REQ-003] Retrieve existing bucket state from Redis hash structure
-- Fields: 'tokens' (current available tokens), 'lastRefill' (timestamp of last refill in ms)
local bucketData = redis.call('HMGET', rateLimitKey, 'tokens', 'lastRefill')

local currentTokens
local lastRefillTimestamp

-- [REQ-003] Initialize bucket if it does not exist in Redis (Lazy Initialization Pattern)
if not bucketData[1] or not bucketData[2] then
    currentTokens = maxCapacity
    lastRefillTimestamp = currentTimeMs
    -- Persist initial state with a TTL safety net (24 hours to prevent memory leaks)
    redis.call('HMSET', rateLimitKey, 'tokens', currentTokens, 'lastRefill', lastRefillTimestamp)
    redis.call('EXPIRE', rateLimitKey, 86400)
else
    currentTokens = tonumber(bucketData[1])
    lastRefillTimestamp = tonumber(bucketData[2])
    
    -- [REQ-003] Compute elapsed time since the last token replenishment
    local elapsedMs = currentTimeMs - lastRefillTimestamp
    
    if elapsedMs > 0 then
        -- Calculate newly generated tokens based on elapsed time and refill rate
        local generatedTokens = elapsedMs * refillRatePerMs
        
        -- Replenish tokens up to the maximum capacity ceiling
        currentTokens = math.min(maxCapacity, currentTokens + generatedTokens)
        
        -- Update the last refill timestamp to the current transaction timestamp
        lastRefillTimestamp = currentTimeMs
    end
end

-- [REQ-003], [EXC-005] Evaluate whether the bucket contains sufficient tokens for the request
if currentTokens >= requestedTokens then
    -- Deduct requested tokens from the bucket balance
    currentTokens = currentTokens - requestedTokens
    
    -- Update Redis state with the new token balance and updated timestamp
    redis.call('HMSET', rateLimitKey, 'tokens', currentTokens, 'lastRefill', lastRefillTimestamp)
    redis.call('EXPIRE', rateLimitKey, 86400)
    
    -- Return success response: [Allowed (1), Remaining Tokens, Retry-After (0)]
    return {1, math.floor(currentTokens), 0}
else
    -- [EXC-005] Rate Limit Exceeded: Calculate exact retry-after duration in seconds
    local deficit = requestedTokens - currentTokens
    local msUntilSufficientTokens = deficit / refillRatePerMs
    local retryAfterSeconds = math.ceil(msUntilSufficientTokens / 1000.0)
    
    -- Ensure retry-after is at least 1 second to prevent immediate spamming
    if retryAfterSeconds < 1 then
        retryAfterSeconds = 1
    end
    
    -- Persist the current state (with potential partial refill) without deducting rejected tokens
    redis.call('HMSET', rateLimitKey, 'tokens', currentTokens, 'lastRefill', lastRefillTimestamp)
    redis.call('EXPIRE', rateLimitKey, 86400)
    
    -- Return failure response: [Rejected (0), Remaining Tokens (0), Retry-After Seconds]
    return {0, math.floor(currentTokens), retryAfterSeconds}
end