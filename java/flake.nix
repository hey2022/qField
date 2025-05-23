{
  description = "Java development environment";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = {
    self,
    nixpkgs,
    flake-utils,
  }: let
    javaVersion = 17;
  in
    {
      overlays.default = final: prev: let
        jdk = prev."jdk${toString javaVersion}";
      in {
        inherit jdk;
        gradle = prev.gradle.override {java = jdk;};
        lombok = prev.lombok.override {inherit jdk;};
      };
    }
    // flake-utils.lib.eachDefaultSystem (
      system: let
        pkgs = import nixpkgs {
          inherit system;
          overlays = [self.overlays.default];
        };
        system-libraries = with pkgs; [
          libGLX
        ];
      in {
        devShells.default = pkgs.mkShell {
          packages = with pkgs; [
            gradle
            jdk
            jdt-language-server
            git
          ];

          shellHook = let
            loadLombok = "-javaagent:${pkgs.lombok}/share/java/lombok.jar";
          in ''
            export JAVA_TOOL_OPTIONS="${loadLombok}''${JAVA_TOOL_OPTIONS:+ $JAVA_TOOL_OPTIONS}"
            export LD_LIBRARY_PATH="${pkgs.lib.makeLibraryPath system-libraries}''${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}"
          '';
        };
      }
    );
}
