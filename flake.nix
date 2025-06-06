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
    flake-utils.lib.eachDefaultSystem (system: let
      pkgs = import nixpkgs {inherit system;};
    in {
      devShells.default = pkgs.mkShell {
        packages = with pkgs; [
          pre-commit
          git
        ];

        shellHook = ''
          if [ ! -f .git/hooks/pre-commit ]; then
            pre-commit install
          fi
        '';
      };
    });
}
