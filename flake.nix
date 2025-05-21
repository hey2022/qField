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
      system-libraries = with pkgs; [
        SDL2
        SDL2_image
        SDL2_mixer
        SDL2_ttf
        freetype
        libjpeg
        libpng
        portmidi
        stdenv.cc.cc.lib
        xorg.libX11
        xorg.libXext
        xorg.libXrandr
        zlib
      ];
    in {
      devShells.default = pkgs.mkShell {
        packages = with pkgs; [
          basedpyright
          git
          pre-commit
          python3
          ruff
          uv
        ];

        shellHook = ''
          export LD_LIBRARY_PATH=${pkgs.lib.makeLibraryPath system-libraries}''${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}
        '';
      };
    });
}
