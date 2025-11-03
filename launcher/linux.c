#include <libgen.h>
#include <stdio.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>

int main() {
    char self_exe[2048];
    memset(self_exe, 0, sizeof(self_exe));
    if (readlink("/proc/self/exe", self_exe, sizeof(self_exe)-1) < 0) {
        perror("readlink");
        return errno;
    }

    char *self_path = dirname(self_exe); // glibc, see bugs: https://man7.org/linux/man-pages/man3/dirname.3.html

    char classpath[2048];
    sprintf(classpath, "%s/lib/*", self_path);

    char prog_home[2048];
    sprintf(prog_home, "-Dprog.home=%s", self_path);

    char *prog = "java";
    char* argv[] = {
        prog,
        "-cp",
        classpath,
        prog_home,
        "io.github.gnush.profiletweaker.MainApp",
        NULL
    };

    execvp(prog, argv);
    printf("Error: %i\n", errno);
}