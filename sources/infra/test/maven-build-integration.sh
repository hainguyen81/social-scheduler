#!/usr/bin/env bash
# [ARC-000] Integration test for Maven build validation of social-scheduler backend modules
set -euo pipefail
IFS=$'
\t'

# Function to log commands and execute them
run_cmd() {
    local cmd="$*"
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] Running: $cmd"
    # Capture version if mvn
    if [[ "$cmd" == mvn* ]]; then
        echo "[$(date +'%Y-%m-%d %H:%M:%S')] Maven version:"
        mvn --version
    fi
    # Execute command
    if ! $cmd; then
        local exit_code=$?
        echo "[$(date +'%Y-%m-%d %H:%M:%S')] Command failed with exit code $exit_code"
        return $exit_code
    fi
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] Command completed with exit code $?"
}

# Step 1: Validate parent pom.xml structure
run_cmd mvn -f ./sources/backend/pom.xml clean validate

# Step 2: Resolve dependencies for the whole project
run_cmd mvn -f ./sources/backend/pom.xml dependency:resolve

# Step 3: Compile user-service
run_cmd mvn -f ./sources/backend/user-service/pom.xml compile

# Step 4: Compile schedule-service
run_cmd mvn -f ./sources/backend/schedule-service/pom.xml compile

# Step 5: Compile ai-service
run_cmd mvn -f ./sources/backend/ai-service/pom.xml compile

# Step 6: Compile rate-limit-service
run_cmd mvn -f ./sources/backend/rate-limit-service/pom.xml compile

echo "[$(date +'%Y-%m-%d %H:%M:%S')] All Maven build steps completed successfully."