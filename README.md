# LameSample
android  studio cmake 编译Lame v3.100 录音MP3测试
# Lame 编译步骤
1. 下载[Lame源码](https://lame.sourceforge.io)这里使用lame 3.100最新版本

2. 在项目cpp目录新建文件夹lame,在解压后文件夹中找到下面的文件复制到cpp/lame里面
    -libmp3lame里的除i386和vector文件夹里全部.h、.c结尾的文件
    -include/lame.h文件
    -libmp3lame/vector/lame_intrin.h和 xmm_quantize_sub.c 文件
    -configMS.h 文件
3. 配置Gradle文件
     externalNativeBuild {
                cmake {
                    cppFlags "-frtti -fexceptions"
                    cFlags "-DSTDC_HEADERS"
                }
            }
4. 配置CMakeLists.txt文件
