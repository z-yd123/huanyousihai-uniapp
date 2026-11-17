#!/usr/bin/env python3
"""把 ruoyi-hys 模块挂载到 ruoyi-springboot（或反向卸载）。

只做两处最小改动，且幂等：
  1. 父 pom <modules> 增加 <module>ruoyi-hys</module>
  2. ruoyi-admin/pom.xml 增加对 ruoyi-hys 的依赖（否则 web 层扫描不到业务 Bean）

不触碰任何框架既有 Java 代码，因此不会与其他同学的分支冲突。
用法：python patch_pom.py <ruoyi-springboot 目录> add|remove
"""
import re
import sys
from pathlib import Path

MODULE = "ruoyi-hys"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def write(path: Path, text: str) -> None:
    path.write_text(text, encoding="utf-8", newline="\n")


def patch_parent(pom: Path, mode: str) -> None:
    text = read(pom)
    has_module = re.search(r"<module>\s*%s\s*</module>" % MODULE, text)
    if mode == "add":
        if has_module:
            print("   父 pom 已包含 ruoyi-hys 模块，跳过")
            return
        text = text.replace("</modules>", "        <module>%s</module>\n    </modules>" % MODULE, 1)
        write(pom, text)
        print("   父 pom 已挂载 ruoyi-hys")
    else:
        if not has_module:
            print("   父 pom 未包含 ruoyi-hys，跳过")
            return
        text = re.sub(r"\s*<module>\s*%s\s*</module>" % MODULE, "", text, count=1)
        write(pom, text)
        print("   父 pom 已移除 ruoyi-hys")


def patch_admin(pom: Path, mode: str) -> None:
    text = read(pom)
    dep = (
        "\n        <!-- 环游四海业务模块（ruoyi-hys） -->\n"
        "        <dependency>\n"
        "            <groupId>com.ruoyi</groupId>\n"
        "            <artifactId>%s</artifactId>\n"
        "            <version>${ruoyi.version}</version>\n"
        "        </dependency>\n" % MODULE
    )
    if mode == "add":
        if "<artifactId>%s</artifactId>" % MODULE in text:
            print("   ruoyi-admin 已依赖 ruoyi-hys，跳过")
            return
        text = text.replace("</dependencies>", dep + "    </dependencies>", 1)
        write(pom, text)
        print("   ruoyi-admin 已增加 ruoyi-hys 依赖")
    else:
        if "<artifactId>%s</artifactId>" % MODULE not in text:
            print("   ruoyi-admin 未依赖 ruoyi-hys，跳过")
            return
        text = re.sub(
            r"\n\s*<!-- 环游四海业务模块[^>]*-->\s*<dependency>\s*<groupId>com\.ruoyi</groupId>\s*"
            r"<artifactId>%s</artifactId>\s*<version>[^<]*</version>\s*</dependency>" % MODULE,
            "",
            text,
            count=1,
        )
        write(pom, text)
        print("   ruoyi-admin 已移除 ruoyi-hys 依赖")


def main() -> int:
    if len(sys.argv) < 3 or sys.argv[2] not in ("add", "remove"):
        print(__doc__)
        return 2
    backend = Path(sys.argv[1]).resolve()
    mode = sys.argv[2]
    patch_parent(backend / "pom.xml", mode)
    patch_admin(backend / "ruoyi-admin" / "pom.xml", mode)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
