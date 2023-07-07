package uk.openvk.android.legacy.api.enumerations;

/** OPENVK LEGACY LICENSE NOTIFICATION
 *
 *  This program is free software: you can redistribute it and/or modify it under the terms of
 *  the GNU Affero General Public License as published by the Free Software Foundation, either
 *  version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 **/

public class HandlerMessages {
    // Authorization (token)
    public static int AUTHORIZED                        =    1;
    public static int INVALID_USERNAME_OR_PASSWORD      =    2;
    public static int TWOFACTOR_CODE_REQUIRED           =    3;

    // Account (/method/Account)
    public static int ACCOUNT_PROFILE_INFO              =  100;
    public static int ACCOUNT_INFO                      =  101;
    public static int ACCOUNT_SET_TO_ONLINE             =  102;
    public static int ACCOUNT_SET_TO_OFFLINE            =  103;
    public static int ACCOUNT_COUNTERS                  =  104;

    // Friends (/method/Friends)
    public static int FRIENDS_GET                       =  200;
    public static int FRIENDS_GET_MORE                  =  201;
    public static int FRIENDS_GET_ALT                   =  202;
    public static int FRIENDS_ADD                       =  203;
    public static int FRIENDS_DELETE                    =  204;
    public static int FRIENDS_CHECK                     =  205;
    public static int FRIENDS_REQUESTS                  =  206;

    // Groups (/method/Groups)
    public static int GROUPS_GET                        =  300;
    public static int GROUPS_GET_MORE                   =  301;
    public static int GROUPS_GET_ALT                    =  302;
    public static int GROUPS_GET_BY_ID                  =  303;
    public static int GROUPS_SEARCH                     =  304;
    public static int GROUPS_JOIN                       =  305;
    public static int GROUPS_LEAVE                      =  306;
    public static int GROUP_MEMBERS                     =  307;

    // Likes (/method/Likes)
    public static int LIKES_ADD                         =  400;
    public static int LIKES_DELETE                      =  401;
    public static int LIKES_CHECK                       =  402;

    // Messages (/method/Messages)
    public static int MESSAGES_GET_BY_ID                =  500;
    public static int MESSAGES_SEND                     =  501;
    public static int MESSAGES_DELETE                   =  502;
    public static int MESSAGES_RESTORE                  =  503;
    public static int MESSAGES_CONVERSATIONS            =  504;
    public static int MESSAGES_GET_CONVERSATIONS_BY_ID  =  505;
    public static int MESSAGES_GET_HISTORY              =  506;
    public static int MESSAGES_GET_LONGPOLL_HISTORY     =  507;
    public static int MESSAGES_GET_LONGPOLL_SERVER      =  508;

    // Users (/method/Users)
    public static int USERS_GET                         =  600;
    public static int USERS_GET_ALT                     =  601;
    public static int USERS_GET_ALT2                    =  602;
    public static int USERS_FOLLOWERS                   =  603;
    public static int USERS_SEARCH                      =  604;

    // Wall (/method/Wall)
    public static int WALL_GET                          =  700;
    public static int WALL_GET_BY_ID                    =  701;
    public static int WALL_GET_MORE                     =  702;
    public static int WALL_POST                         =  703;
    public static int WALL_REPOST                       =  704;
    public static int WALL_CREATE_COMMENT               =  705;
    public static int WALL_DELETE_COMMENT               =  706;
    public static int WALL_COMMENT                      =  707;
    public static int WALL_ALL_COMMENTS                 =  708;

    // Newsfeed (/method/Newsfeed)
    public static int NEWSFEED_GET                      =  800;
    public static int NEWSFEED_GET_GLOBAL               =  801;
    public static int NEWSFEED_GET_MORE                 =  802;
    public static int NEWSFEED_GET_MORE_GLOBAL          =  803;

    // Notes (/method/Notes)
    public static int NOTES_GET                         =  900;
    public static int NOTES_GET_BY_ID                   =  901;

    // OpenVK specific (/method/Ovk)
    public static int OVK_VERSION                       = 1000;
    public static int OVK_TEST                          = 1001;
    public static int OVK_CHICKEN_WINGS                 = 1002;
    public static int OVK_ABOUTINSTANCE                 = 1003;
    public static int OVK_CHECK_HTTP                    = 1004;
    public static int OVK_CHECK_HTTPS                   = 1005;

    // Poll (/method/Poll)
    public static int POLL_ADD_VOTE                     = 1100;
    public static int POLL_DELETE_VOTE                  = 1101;

    // Misc (LongPoll API, avatars, attachments and etc.)
    public static int ACCOUNT_AVATAR                    = 2000;
    public static int NEWSFEED_ATTACHMENTS              = 2001;
    public static int WALL_ATTACHMENTS                  = 2002;
    public static int WALL_AVATARS                      = 2003;
    public static int NEWSFEED_AVATARS                  = 2004;
    public static int PROFILE_AVATARS                   = 2005;
    public static int GROUP_AVATARS                     = 2006;
    public static int GROUP_AVATARS_ALT                 = 2007;
    public static int FRIEND_AVATARS                    = 2008;
    public static int COMMENT_AVATARS                   = 2009;
    public static int COMMENT_PHOTOS                    = 2010;
    public static int COMMENT_VIDEO_THUMBNAILS          = 2011;
    public static int CONVERSATIONS_AVATARS             = 2012;
    public static int LONGPOLL                          = 2013;
    public static int ORIGINAL_PHOTO                    = 2014;
    public static int VIDEO_THUMBNAILS                  = 2015;
    public static int PARSE_JSON                        = 2016;

    // Errors
    public static int NO_INTERNET_CONNECTION            =   -1;
    public static int CONNECTION_TIMEOUT                =   -2;
    public static int INVALID_JSON_RESPONSE             =   -3;
    public static int INVALID_USAGE                     =   -4;
    public static int INVALID_TOKEN                     =   -5;
    public static int CHAT_DISABLED                     =   -6;
    public static int METHOD_NOT_FOUND                  =   -7;
    public static int BANNED_ACCOUNT                    =   -8;
    public static int ACCESS_DENIED                     =   -9;
    public static int ACCESS_DENIED_MARSHMALLOW         =   -10;
    public static int BROKEN_SSL_CONNECTION             =   -11;
    public static int INTERNAL_ERROR                    =   -12;
    public static int INSTANCE_UNAVAILABLE              =   -13;
    public static int NOT_OPENVK_INSTANCE               =   -14;
    public static int UNKNOWN_ERROR                     =   -15;
}
