{
  description = "qField flake";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = {
    nixpkgs,
    flake-utils,
    ...
  }:
    flake-utils.lib.eachDefaultSystem (
      system: let
        overlays = [
          (final: prev: {
            python3 = prev.python3.override {
              packageOverrides = python-final: python-prev: {
                pygame = python-prev.pygame.overridePythonAttrs (old: {
                  env.PYGAME_DETECT_AVX2 = "1";
                });
              };
            };
          })
        ];

        pkgs = import nixpkgs {
          inherit system overlays;
        };
      in {
        devShells.default = pkgs.mkShell {
          packages = with pkgs; [
            (python3.withPackages (python-packages:
              with python-packages; [
                pygame

                pip
                pytest
              ]))
            basedpyright
            ruff
            git
          ];
        };
      }
    );
}
