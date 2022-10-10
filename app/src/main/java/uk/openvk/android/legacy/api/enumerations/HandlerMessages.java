package uk.openvk.android.legacy.api.enumerations;

public class HandlerMessages {
    // Authorization
    public static int AUTHORIZED                        =   1;
    public static int INVALID_USERNAME_OR_PASSWORD      =   2;
    public static int TWOFACTOR_CODE_REQUIRED           =   3;

    // Account
    public static int ACCOUNT_PROFILE_INFO              = 100;
    public static int ACCOUNT_INFO                      = 101;
    public static int ACCOUNT_SET_TO_ONLINE             = 102;
    public static int ACCOUNT_SET_TO_OFFLINE            = 103;
    public static int ACCOUNT_COUNTERS                  = 104;

    // Friends
    public static int FRIENDS_GET                       = 200;
    public static int FRIENDS_GET_ALT                   = 201;
    public static int FRIENDS_ADD                       = 202;
    public static int FRIENDS_DELETE                    = 203;
    public static int FRIENDS_CHECK                     = 204;

    // Groups
    public static int GROUPS_GET                        = 300;
    public static int GROUPS_GET_ALT                    = 301;
    public static int GROUPS_GET_BY_ID                  = 302;
    public static int GROUPS_SEARCH                     = 303;

    // Likes
    public static int LIKES_ADD                         = 400;
    public static int LIKES_DELETE                      = 401;
    public static int LIKES_CHECK                       = 402;

    // Messages
    public static int MESSAGES_GET_BY_ID                = 500;
    public static int MESSAGES_SEND                     = 501;
    public static int MESSAGES_DELETE                   = 502;
    public static int MESSAGES_RESTORE                  = 503;
    public static int MESSAGES_CONVERSATIONS            = 504;
    public static int MESSAGES_GET_CONVERSATIONS_BY_ID  = 505;
    public static int MESSAGES_GET_HISTORY              = 506;
    public static int MESSAGES_GET_LONGPOLL_HISTORY     = 507;
    public static int MESSAGES_GET_LONGPOLL_SERVER      = 508;

    // Users
    public static int USERS_GET                         = 600;
    public static int USERS_FOLLOWERS                   = 601;
    public static int USERS_SEARCH                      = 602;

    // Wall
    public static int WALL_GET                          = 700;
    public static int WALL_GET_BY_ID                    = 701;
    public static int WALL_POST                         = 702;
    public static int WALL_REPOST                       = 703;
    public static int WALL_CREATE_COMMENT               = 704;
    public static int WALL_DELETE_COMMENT               = 705;
    public static int WALL_COMMENT                      = 706;
    public static int WALL_ALL_COMMENTS                 = 707;

    // Newsfeed
    public static int NEWSFEED_GET                      = 800;

    // OpenVK specific
    public static int OVK_VERSION                       = 900;
    public static int OVK_TEST                          = 901;
    public static int OVK_CHICKEN_WINGS                 = 902;
    public static int OVK_ABOUTINSTANCE                 = 903;
    public static int OVK_CHECK_HTTP                    = 904;
    public static int OVK_CHECK_HTTPS                   = 905;

    // Misc
    public static int NEWSFEED_ATTACHMENTS              = 1000;
    public static int WALL_ATTACHMENTS                  = 1002;
    public static int WALL_AVATARS                      = 1003;
    public static int NEWSFEED_AVATARS                  = 1004;
    public static int PROFILE_AVATARS                   = 1005;
    public static int GROUP_AVATARS                     = 1006;
    public static int GROUP_AVATARS_ALT                 = 1007;
    public static int FRIEND_AVATARS                    = 1008;
    public static int COMMENT_AVATARS                   = 1009;
    public static int CONVERSATION_AVATARS              = 1010;

    // Errors
    public static int NO_INTERNET_CONNECTION            =  -1;
    public static int CONNECTION_TIMEOUT                =  -2;
    public static int INVALID_JSON_RESPONSE             =  -3;
    public static int INVALID_USAGE                     =  -4;
    public static int INVALID_TOKEN                     =  -5;
    public static int METHOD_NOT_FOUND                  =  -6;
    public static int BROKEN_SSL_CONNECTION             =  -7;
    public static int INTERNAL_ERROR                    =  -8;
}
