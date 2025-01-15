{
  description = "qField flake";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
    pyproject-nix = {
      url = "github:nix-community/pyproject.nix";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };

  outputs = {
    nixpkgs,
    flake-utils,
    pyproject-nix,
    ...
  }:
    flake-utils.lib.eachDefaultSystem (system: let
      overlays = [
        (final: prev: {
          python3 = prev.python3.override {
            packageOverrides = _: python-prev: {
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

      # Setup from pyproject.toml
      python = pkgs.python3;
      project = pyproject-nix.lib.project.loadPyproject {
        projectRoot = ./.;
      };
      python-env = python.withPackages (project.renderers.withPackages {
        inherit python;
      });
    in {
      devShells.default = pkgs.mkShell {
        packages = with pkgs; [
          python-env

          basedpyright
          uv
          ruff
          git
        ];
      };
    });
}
