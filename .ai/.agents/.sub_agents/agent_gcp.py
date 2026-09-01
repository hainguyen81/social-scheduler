# .ai/.agents/.sub-agents/agent-gcp.py
import os
import subprocess
import sys

# super agent
from _0d_ai._0d_agents._0d_sub_0u_agents.agent_0u_super import AbstractSubAgent

# ==============================================================================
# 🏢 ENTERPRISE INTER-PACKAGE ROUTING LAYER
# ==============================================================================
# Programmatically appends the parent directory (.ai/.agents/) into Python's runtime
# search path array. This completely unlocks importing 'agent_helper.py'.
# ==============================================================================
# request agent_helper from `.libs/project_agents_package_loader.py`
from _0d_ai._0d_agents.agent_0u_helper import (
    kwargs_by_key,
    parse_args,
    resolve_absolute_path,
)

# ==============================================================================
# GLOBAL CONFIGURATION PATHS - CONFIG HERE TO CUSTOMIZE DIRECTORY STRUCTURE
# ==============================================================================
AGENT_ID                    = "GCP"
AGENT_NAME                  = "🤖☁️ EnterpriseGCPDeployerAgent"
BACKEND_DOCKERFILE          = resolve_absolute_path("sources/backend/src/main/docker/Dockerfile.native")
FRONTEND_DOCKERFILE         = resolve_absolute_path("sources/frontend/Dockerfile")

class GcpAgent(AbstractSubAgent):
    def __init__(self, phase_str, day_num):
        super().__init__(
            agent_id=AGENT_ID,
            agent_name=AGENT_NAME,
            phase_str=phase_str,
            day_num=day_num
        )

    def authenticate_gcp(self):
        self.logger.info("ℹ️ Authenticating context with Google Cloud Platform SDK...")
        if self.gcp_sa_key and self.gcp_project and self.gcp_region:
            with open("gcp-key.json", "w", encoding="utf-8") as f:
                f.write(self.gcp_sa_key)
            subprocess.run(["gcloud", "auth", "activate-service-account", f"--key-file=gcp-key.json"], check=True)
            subprocess.run(["gcloud", "config", "set", "project", self.gcp_project], check=True)
            subprocess.run(["gcloud", "auth", "configure-docker", f"{self.gcp_region}-docker.pkg.dev"], check=True)
            os.remove("gcp-key.json")
        else:
            self.logger.warning("⚠️ Missing parameters inside GCP_SECRETS. Relying on active local shell auth context.")
    
    def gcp_cloud_repo(self) -> str:
        return os.environ.get("GCP_REPO")
    
    def gcp_cloud_project(self) -> str:
        return self.agent_secrets("GCP_PROJECT_ID")
    
    def gcp_cloud_region(self) -> str:
        return self.agent_secrets("GCP_REGION")
    
    def gcp_cloud_sa_key(self) -> str:
        return self.agent_secrets("GCP_SA_KEY")
    
    def gcp_cloud_image(self) -> str:
        return f"{self.gcp_region}-docker.pkg.dev/{self.gcp_project}/{self.gcp_repo}:{self.image_tag}"
    
    # @override
    def initialize(self):
        super().initialize()
        self.image_tag = f"day-{self.day_num}"
        self.gcp_repo = self.gcp_cloud_repo()
        self.gcp_project = self.gcp_cloud_project()
        self.gcp_region = self.gcp_cloud_region()
        self.gcp_image = self.gcp_cloud_image()
        self.gcp_sa_key = self.gcp_cloud_sa_key()
        
    # @override
    def initialize_models(self):
        pass
    
    # @override
    def agent_secrets_key(self) -> str:
        return "GCP_SECRETS"
    
    # @override
    def agent_log_file(self) -> str:
        return resolve_absolute_path(f".ai/.history/agent-gcp-phase-{self.phase_str}-day-{self.day_num}.md")
    
    # @override
    def system_prompt_template(self) -> str:
        return None
    
    # @override
    def user_prompt_template(self) -> str:
        return None
    
    # @ override
    def pre_execute(self, **kwargs):
        # validate repository
        if not self.gcp_repo or len(self.gcp_repo.strip()) <= 0:
            self.logger.warning("⚠️ Not found 'GCP_REPO' enviroment to publish image for deploying.")
            sys.exit(0)
        
        # log-in repository
        self.authenticate_gcp()
        
        # return kwargs
        return super().pre_execute(**kwargs)

    # @ override
    def __execute__(self, **kwargs):
        # extract arguments
        target_component = kwargs_by_key(key="target_component", **kwargs)
        
        # check task for backend or frontend
        is_backend = "backend" in target_component
        dockerfile_path = BACKEND_DOCKERFILE if is_backend else FRONTEND_DOCKERFILE
        workspace_path = resolve_absolute_path("sources/backend") if is_backend else resolve_absolute_path("sources/frontend")
        
        if not os.path.exists(dockerfile_path):
            self.logger.warning(f"⚠️ Target container instruction blueprint absent at: {dockerfile_path}")
            return (True, None, None, f"⚠️ Target container instruction blueprint absent at: {dockerfile_path}")
        
        # build image
        self.logger.info(f"ℹ️ Compiling multi-stage container artifact: {self.gcp_image}")
        subprocess.run(["docker", "build", "-t", self.gcp_image, "-f", dockerfile_path, workspace_path], check=True)

        self.logger.info("ℹ️ Uploading image binary up to Google Artifact Registry...")
        subprocess.run(["docker", "push", self.gcp_image], check=True)
        self.logger.info(f"✅ Image version {self.image_tag} published safely to GAR!")
        
        # result
        return {
            **kwargs,
            "system_prompt": None,
            "user_prompt": None,
            "raw_response": f"✅ Image version {self.image_tag} published safely to GAR!"
        }

if __name__ == "__main__":
    def add_known_arguments(parser):
        parser.add_argument("--phase", required=True)
        parser.add_argument("--day", required=True)
    
    args, unknown_args = parse_args(
        description=AGENT_ID,
        parser_callback=add_known_arguments
    )
    
    print(f"☁️ Connecting to Google Cloud SDK components to route compiled GAR assets for Phase { args.phase } Day { args.day }...")
    GcpAgent(
        phase_str=args.phase,
        day_num=args.day
    ).execute()
