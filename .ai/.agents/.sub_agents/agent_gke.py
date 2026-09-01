# .ai/.agents/.sub-agents/agent-gke.py
import subprocess
import sys

# super agent
from _0d_ai._0d_agents._0d_sub_0u_agents.agent_0u_gcp import GcpAgent

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
AGENT_ID    = "GKE"
AGENT_NAME  = "🤖☸️ EnterpriseGKEDeployerAgent"

class GkeAgent(GcpAgent):
    def __init__(self, phase_str, day_num):
        super().__init__(
            agent_id=AGENT_ID,
            agent_name=AGENT_NAME,
            phase_str=phase_str,
            day_num=day_num
        )
    
    def authenticate_gcp(self):
        self.configure_gke_credentials()
    
    def configure_gke_credentials(self):
        if self.gke_cluster_name and self.gcp_region and self.gcp_project:
            self.logger.info(f"ℹ️ Fetching security credentials context for cluster registry: {self.gke_cluster_name}")
            subprocess.run([
                "gcloud", "container", "clusters", "get-credentials", self.gke_cluster_name,
                f"--region={self.gcp_region}", f"--project={self.gcp_project}"
            ], check=True)
        else:
            self.logger.warning("⚠️ Missing data keys inside GKE_SECRETS array map framework parameters.")
    
    def gke_cloud_deployment_name(self) -> str:
        return self.agent_secrets("GKE_DEPLOYMENT_NAME")
    
    def gke_cloud_cluster(self) -> str:
        return self.agent_secrets("GKE_CLUSTER_NAME")
    
    # @override
    def initialize(self):
        super().initialize()
        self.gke_deployment_name = self.gke_cloud_deployment_name()
        self.gke_cluster_name = self.gke_cloud_cluster()
    
    # @override
    def agent_secrets_key(self) -> str:
        return "GKE_SECRETS"
    
    # @override
    def agent_log_file(self) -> str:
        return resolve_absolute_path(f".ai/.history/agent-gke-phase-{self.phase_str}-day-{self.day_num}.md")
    
    # @ override
    def pre_execute(self, **kwargs):
        # validate repository
        if not self.gke_deployment_name or len(self.gke_deployment_name.strip()) <= 0:
            self.logger.warning("⚠️ Not found 'GKE_DEPLOYMENT_NAME' enviroment. Step is explicitly marked as 'none'. Skipping GKE cluster rollout update loops framework entirely.")
            sys.exit(0)
        
        # as super
        return super().pre_execute()

    # @ override
    def __execute__(self, **kwargs):
        # extract arguments
        target_component = kwargs_by_key(key="target_component", **kwargs)
        
        # Standard Microservice Application Rollout Logic using your custom prefixed parameters name (e.g. gke-membership-hub-backend)
        is_backend = "backend" in target_component
        app_domain = f"{self.project_name}-backend" if is_backend else "{self.project_name}-frontend"
        
        # Check if the target day represents a dedicated infrastructure day targeting raw K8s deployment manifests (like Day 23)
        if "infrastructure/k8s" in target_component:
            self.logger.info(f"ℹ️ Applying raw enterprise infrastructure update manifests: {target_component}")
            target_component = resolve_absolute_path(target_component)
            subprocess.run(["kubectl", "apply", "-f", target_component], check=True)
            self.logger.info("✅ Cloud infrastructure manifest rules applied securely on GKE compute pools!")
            
            # result
            return {
                **kwargs,
                "system_prompt": None,
                "user_prompt": None,
                "latest_system_prompt": None,
                "latest_user_prompt": None,
                "raw_response": "✅ Cloud infrastructure manifest rules applied securely on GKE compute pools!"
            }
        
        self.logger.info(f"ℹ️ Activating safe, zero-downtime rolling update across container workloads for deployment: {self.gke_deployment_name}")
        subprocess.run([
            "kubectl", "set", "image", f"deployment/{self.gke_deployment_name}",
            f"{app_domain}-container={self.gcp_image}"
        ], check=True)
        
        subprocess.run(["kubectl", "rollout", "status", f"deployment/{self.gke_deployment_name}"], check=True)
        self.logger.info(f"✅ Successfully deployed container version {self.image_tag} to GKE pods clusters!")
        
        # result
        return {
            **kwargs,
            "system_prompt": None,
            "user_prompt": None,
            "raw_response": f"✅ Successfully deployed container version {self.image_tag} to GKE pods clusters!"
        }

if __name__ == "__main__":
    def add_known_arguments(parser):
        parser.add_argument("--phase", required=True)
        parser.add_argument("--day", required=True)
    
    args, unknown_args = parse_args(
        description=AGENT_ID,
        parser_callback=add_known_arguments
    )
    
    print(f"☸️ Initiating secure handshakes towards remote GKE cluster pools for Phase { args.phase } Day { args.day }...")
    GkeAgent(
        phase_str=args.phase,
        day_num=args.day
    ).execute()
