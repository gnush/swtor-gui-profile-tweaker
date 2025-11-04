#include <windows.h>
#include <stdio.h>
#include <process.h>
#include <errno.h>
#include <string.h>

int main() {
    char **self_exe;
    _get_pgmptr(self_exe);

    char self_drive[4];
    char self_dir[1024];

    _splitpath_s(
        *self_exe,
        self_drive, sizeof(self_drive),
        self_dir, sizeof(self_dir),
        NULL, 0, // file name
        NULL, 0  // file extension
    );

    char classpath[1032];
    sprintf(classpath, "%s%slib\\*", self_drive, self_dir);

    char prog_home[1040];
    sprintf(prog_home, "-Dprog.home=%s%s", self_drive, self_dir);

    char *prog = "java.exe";
    const char* const argv[] = {
        prog,
        "-cp",
        classpath,
        prog_home,
        "io.github.gnush.profiletweaker.MainApp",
        NULL
    };

    _spawnvp(_P_OVERLAY, prog, argv);
    perror("_spawnvp");
}