#ifndef _OVKMPLAY_EXTCPP_H
    #define _OVKMPLAY_EXTCPP_H

    #pragma clang diagnostic push
    extern "C" {
        #ifdef __cplusplus
            #define __STDC_CONSTANT_MACROS
            #ifdef _STDINT_H
                #undef _STDINT_H
            #endif
            #include <stdint.h>
        #endif
    }

    #ifndef INT64_C
        #define INT64_C(c)  (c ## LL)
        #define UINT64_C(c) (c ## ULL)
    #endif
#endif