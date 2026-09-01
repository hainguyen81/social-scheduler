# .ai/.agents/.sub-agents/agent-docker.py
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
AGENT_ID                    = "Docker"
AGENT_NAME                  = "🤖🐳 EnterpriseDockerDeployerAgent"
BACKEND_DOCKERFILE          = resolve_absolute_path("sources/backend/src/main/docker/Dockerfile.native")
FRONTEND_DOCKERFILE         = resolve_absolute_path("sources/frontend/Dockerfile")

class DockerHubAgent(AbstractSubAgent):
    def __init__(self, phase_str, day_num):
        super().__init__(
            agent_id=AGENT_ID,
            agent_name=AGENT_NAME,
            phase_str=phase_str,
            day_num=day_num
        )

    def authenticate_dockerhub(self):
        self.logger.info("ℹ️ Attaching secure registry authorization handshakes...")
        username = self.agent_secrets("DOCKERHUB_USERNAME")
        password = self.agent_secrets("DOCKERHUB_PASSWORD")

        if username and password:
            login_process = subprocess.Popen(
                ["docker", "login", "-u", username, "--password-stdin"],
                stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True
            )
            _, stderr = login_process.communicate(input=password)
            if login_process.returncode != 0:
                self.logger.critical(f"💀 Authentication verification failed natively: {stderr}")
                sys.exit(1)
            self.logger.info("✅ Docker Hub authentication session activated successfully.")
        else:
            self.logger.warning("⚠️ Missing data keys parameters inside DOCKERHUB_SECRETS mapping registry.")
    
    def docker_hub_repo(self) -> str:
        return self.agent_secrets("DOCKERHUB_REPO")
    
    def docker_hub_namespace(self) -> str:
        return self.agent_secrets("DOCKERHUB_NAMESPACE", self.agent_secrets("DOCKERHUB_USERNAME"))
    
    def docker_hub_image(self) -> str:
        return f"{self.docker_namespace}/{self.docker_repo}:{self.image_tag}"
    
    # @override
    def initialize(self):
        super().initialize()
        self.image_tag = f"day-{self.day_num}"
        self.docker_repo = self.docker_hub_repo()
        self.docker_namespace = self.docker_hub_namespace()
        self.docker_image = self.docker_hub_image()
        
    # @override
    def initialize_models(self):
        pass
    
    # @override
    def agent_secrets_key(self) -> str:
        return "DOCKERHUB_SECRETS"
    
    # @override
    def agent_log_file(self) -> str:
        return resolve_absolute_path(f".ai/.history/agent-docker-phase-{self.phase_str}-day-{self.day_num}.md")
    
    # @override
    def system_prompt_template(self) -> str:
        return None
    
    # @override
    def user_prompt_template(self) -> str:
        return None
    
    # @ override
    def pre_execute(self, **kwargs):
        # validate repository
        if not self.docker_repo or len(self.docker_repo.strip()) <= 0:
            self.logger.warning("⚠️ Not found 'DOCKERHUB_REPO' enviroment to publish docker images.")
            sys.exit(0)
        
        # log-in repository
        self.authenticate_dockerhub()
        
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
        
        # check whether exists docker file
        if not os.path.exists(dockerfile_path):
            self.logger.warning(f"⚠️ Target container instruction blueprint absent at: {dockerfile_path}")
            return (True, None, None, f"⚠️ Target container instruction blueprint absent at: {dockerfile_path}")
        
        # build image
        self.logger.info(f"ℹ️ Packaging multi-stage application image component: {self.docker_image}")
        subprocess.run(["docker", "build", "-t", self.docker_image, "-f", dockerfile_path, workspace_path], check=True)
        
        # push image to DockerHub
        self.logger.info("ℹ️ Streaming production release tag across remote Docker Hub brokers pipelines...")
        subprocess.run(["docker", "push", self.docker_image], check=True)
        self.logger.info(f"✅ Image package {self.docker_image} successfully committed upstream!")
        
        # result
        return {
            **kwargs,
            "system_prompt": None,
            "user_prompt": None,
            "raw_response": f"✅ Image package {self.docker_image} successfully committed upstream!"
        }

if __name__ == "__main__":
    def add_known_arguments(parser):
        parser.add_argument("--phase", required=True)
        parser.add_argument("--day", required=True)
    
    args, unknown_args = parse_args(
        description=AGENT_ID,
        parser_callback=add_known_arguments
    )
    
    print(f"🐳 Launching Docker Hub container build and registry publication pipes for Phase { args.phase } Day { args.day }...")
    DockerHubAgent(
        phase_str=args.phase,
        day_num=args.day
    ).execute()
