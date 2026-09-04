LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_LDLIBS := -llog
LOCAL_MODULE := local-socket
LOCAL_SRC_FILES := local-socket.cpp
# Android 15+ devices can boot with a 16 KB page size, and a shared library whose
# LOAD segments are aligned to the old 4 KB page size fails to load on them.
# NDK r27+ does this by default, but JitPack builds override the NDK version via
# JITPACK_NDK_VERSION, so set it explicitly to stay correct on older NDKs too.
LOCAL_LDFLAGS += -Wl,-z,max-page-size=16384

include $(BUILD_SHARED_LIBRARY)
