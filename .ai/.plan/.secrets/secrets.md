### 1. AI MODELS CONFIGURATION (CENTRALIZED JSON)
*   **`AI_MODELS_KEYS_JSON`**
    *   *Description:* 1 tệp JSON duy nhất chứa toàn bộ API Keys của các mô hình AI phục vụ cơ chế Failover.
    *   *Format to paste into GitHub Secrets:*
        ```json
        {
          "gemini-2.5-pro": "AIzaSyYourActualGoogleStudioApiKeyHere",
          "gemini-2.5-flash": "AIzaSyYourActualGoogleStudioApiKeyHere",
          "deepseek-coder": "sk-yourActualDeepSeekPlatformApiKeyHere",
          "gpt-4o-mini": "sk-proj-yourActualOpenAIApiKeyHere",
          "qwen-coder-32b-instruct": "gsk_yourActualGroqConsoleApiKeyHere"
        }
        ```

### 2. GOOGLE CLOUD PLATFORM INFRASTRUCTURE (GCP & GKE AGENTS)
*   **`GCP_SA_KEY`**
    *   *Description:* Toàn bộ nội dung file JSON của Google Cloud Service Account (chứa quyền Artifact Registry Admin, Kubernetes Engine Developer). Copy nguyên block JSON của file key vào.
*   **`GCP_PROJECT_ID`** *(Nên add vào Repository Variables)*
    *   *Description:* ID của dự án Google Cloud (Ví dụ: `nlh4j-saas-membership`).
*   **`GCP_REGION`** *(Nên add vào Repository Variables)*
    *   *Description:* Vùng đặt hạ tầng (Ví dụ: `asia-southeast1`).
*   **`GKE_CLUSTER_NAME`** *(Nên add vào Repository Variables)*
    *   *Description:* Tên cụm máy chủ Kubernetes đang chạy trên GCP (Ví dụ: `membership-hub-gke`).
*   **`GAR_REPOSITORY`** *(Nên add vào Repository Variables)*
    *   *Description:* Tên kho lưu trữ Docker Images trên Google Artifact Registry (Ví dụ: `membership-hub-registry`).

#### 2.1. GCP INFRASTRUCTURE SECRETS (CENTRALIZED JSON)
*   **`GCP_SECRETS`**
    *   *Description:* Lưu trữ khóa bảo mật tài khoản dịch vụ và thông tin định danh Registry của Google Cloud Platform.
    *   *Format to paste into GitHub Secrets:*
        ```json
        {
          "GCP_SA_KEY": "{\"type\": \"service_account\", \"project_id\": \"nlh4j-saas-membership\", ...}",
          "GCP_PROJECT_ID": "nlh4j-saas-membership",
          "GCP_REGION": "asia-southeast1",
          "GAR_REPOSITORY": "membership-hub-registry"
        }
        ```

#### 2.2. GKE CONTAINER ORCHESTRATION SECRETS (CENTRALIZED JSON)
*   **`GKE_SECRETS`**
    *   *Description:* Lưu trữ thông số kết nối và điều phối cụm máy chủ Kubernetes Engine.
    *   *Format to paste into GitHub Secrets:*
        ```json
        {
          "GCP_PROJECT_ID": "nlh4j-saas-membership",
          "GCP_REGION": "asia-southeast1",
          "GKE_CLUSTER_NAME": "membership-hub-gke",
          "GAR_REPOSITORY": "membership-hub-registry"
        }
        ```

#### 2.3. DOCKER HUB REGISTRY SECRETS (CENTRALIZED JSON)
*   **`DOCKERHUB_SECRETS`**
    *   *Description:* Khối JSON lưu trữ thông tin tài khoản và namespace để xác thực đẩy ảnh container lên Docker Hub.
    *   *Format to paste into GitHub Secrets:*
        ```json
        {
          "DOCKERHUB_USERNAME": "your_dockerhub_username",
          "DOCKERHUB_PASSWORD": "your_dockerhub_access_token_or_password",
          "DOCKERHUB_NAMESPACE": "your_organization_or_username"
        }
        ```

### 3. CRM CHANNELS & EXTERNAL NOTIFICATIONS INTEGRATIONS

#### 3.1. CRM CHANNELS & EXTERNAL NOTIFICATIONS INTEGRATIONS
*   **`ZALO_OA_ACCESS_TOKEN`**
    *   *Description:* Token cấp quyền gọi API Zalo OA để gửi tin nhắn điểm danh cá nhân và nhắc nợ 3 ngày.
*   **`ZALO_OA_SECRET_KEY`**
    *   *Description:* Khóa bí mật ứng dụng Zalo để ký chữ ký số (Signature) khi Backend kết nối Zalo Group CRM.
*   **ZALO OPERATIONAL SECRETS (CENTRALIZED JSON)`**
*   **`ZALO_OA_SECRETS`**
    *   *Description:* Khối JSON tập trung lưu trữ các mã khóa ủy quyền, chữ ký số và token truy cập cho phân hệ Zalo Official Account & CRM Group.
    *   *Format to paste into GitHub Secrets:*
        ```json
        {
          "ZALO_OA_ACCESS_TOKEN": "yourActualZaloOaAccessTokenStringHere",
          "ZALO_OA_SECRET_KEY": "yourActualZaloAppSecretKeyStringHere"
        }
        ```

#### 3.2. MOBILE PUSH NOTIFICATIONS SECRETS (CENTRALIZED JSON)
*   **`FIREBASE_SECRETS`**
    *   *Description:* Khối JSON lưu trữ thông tin Service Account tối cao cấp quyền đẩy thông báo real-time của Google Firebase.
    *   *Format to paste into GitHub Secrets:*
        ```json
        {
          "FIREBASE_SERVICE_ACCOUNT_JSON": "{\"type\": \"service_account\", \"project_id\": \"your-firebase-id\", ...}"
        }
        ```



