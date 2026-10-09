/*
 *  Copyleft © 2022-24, 2026 OpenVK Team
 *  Copyleft © 2022-24, 2026 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This file is part of OpenVK API Client Library for Android.
 *
 *  OpenVK API Client Library for Android is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along
 *  with this program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 */

package uk.openvk.android.client.enumerations;

public class HandlerMessages {
    // Authorization (token)
    public final static int AUTHORIZED                        =    1;
    public final static int INVALID_USERNAME_OR_PASSWORD      =    2;
    public final static int TWOFACTOR_CODE_REQUIRED           =    3;

    // Account (/method/Account)
    public final static int ACCOUNT_PROFILE_INFO              =  100;
    public final static int ACCOUNT_INFO                      =  101;
    public final static int ACCOUNT_SET_TO_ONLINE             =  102;
    public final static int ACCOUNT_SET_TO_OFFLINE            =  103;
    public final static int ACCOUNT_COUNTERS                  =  104;

    // Friends (/method/Friends)
    public final static int FRIENDS_GET                       =  200;
    public final static int FRIENDS_GET_MORE                  =  201;
    public final static int FRIENDS_GET_ALT                   =  202;
    public final static int FRIENDS_ADD                       =  203;
    public final static int FRIENDS_DELETE                    =  204;
    public final static int FRIENDS_CHECK                     =  205;
    public final static int FRIENDS_REQUESTS                  =  206;

    // Groups (/method/Groups)
    public final static int GROUPS_GET                        =  300;
    public final static int GROUPS_GET_MORE                   =  301;
    public final static int GROUPS_GET_ALT                    =  302;
    public final static int GROUPS_GET_BY_ID                  =  303;
    public final static int GROUPS_SEARCH                     =  304;
    public final static int GROUPS_JOIN                       =  305;
    public final static int GROUPS_LEAVE                      =  306;
    public final static int GROUP_MEMBERS                     =  307;

    // Likes (/method/Likes)
    public final static int LIKES_ADD                         =  400;
    public final static int LIKES_DELETE                      =  401;
    public final static int LIKES_CHECK                       =  402;

    // Messages (/method/Messages)
    public final static int MESSAGES_GET_BY_ID                =  500;
    public final static int MESSAGES_SEND                     =  501;
    public final static int MESSAGES_DELETE                   =  502;
    public final static int MESSAGES_RESTORE                  =  503;
    public final static int MESSAGES_CONVERSATIONS            =  504;
    public final static int MESSAGES_GET_CONVERSATIONS_BY_ID  =  505;
    public final static int MESSAGES_GET_HISTORY              =  506;
    public final static int MESSAGES_GET_LONGPOLL_HISTORY     =  507;
    public final static int MESSAGES_GET_LONGPOLL_SERVER      =  508;

    // Users (/method/Users)
    public final static int USERS_GET                         =  600;
    public final static int USERS_GET_ALT                     =  601;
    public final static int USERS_GET_ALT2                    =  602;
    public final static int USERS_FOLLOWERS                   =  603;
    public final static int USERS_SEARCH                      =  604;

    // Wall (/method/Wall)
    public final static int WALL_GET                          =  700;
    public final static int WALL_GET_BY_ID                    =  701;
    public final static int WALL_GET_MORE                     =  702;
    public final static int WALL_POST                         =  703;
    public final static int WALL_REPOST                       =  704;
    public final static int WALL_CREATE_COMMENT               =  705;
    public final static int WALL_DELETE_COMMENT               =  706;
    public final static int WALL_COMMENT                      =  707;
    public final static int WALL_ALL_COMMENTS                 =  708;

    // Newsfeed (/method/Newsfeed)
    public final static int NEWSFEED_GET                      =  800;
    public final static int NEWSFEED_GET_GLOBAL               =  801;
    public final static int NEWSFEED_GET_MORE                 =  802;
    public final static int NEWSFEED_GET_MORE_GLOBAL          =  803;

    // Notes (/method/Notes)
    public final static int NOTES_ADD                         =  900;
    public final static int NOTES_GET                         =  901;
    public final static int NOTES_GET_BY_ID                   =  902;
    public final static int NOTES_EDIT                        =  903;

    // Photos (/method/Photos)
    public final static int PHOTOS_GET                        = 1000;
    public final static int PHOTOS_UPLOAD_SERVER              = 1001;
    public final static int PHOTOS_SAVE                       = 1002;
    public final static int PHOTOS_GETALBUMS                  = 1003;

    // Videos (/method/Video)
    public final static int VIDEOS_GET                        = 1100;

    // Audios (/method/Audio)
    public final static int AUDIOS_GET                        = 1200;
    public final static int AUDIOS_GET_LYRICS                 = 1201;
    public final static int AUDIOS_SEARCH                     = 1202;

    // OpenVK specific (/method/Ovk)
    public final static int OVK_VERSION                       = 1300;
    public final static int OVK_TEST                          = 1301;
    public final static int OVK_CHICKEN_WINGS                 = 1302;
    public final static int OVK_ABOUTINSTANCE                 = 1303;
    public final static int OVK_CHECK_HTTP                    = 1304;
    public final static int OVK_CHECK_HTTPS                   = 1305;

    // Poll (/method/Poll)
    public final static int POLL_ADD_VOTE                     = 1400;
    public final static int POLL_DELETE_VOTE                  = 1401;

    // Misc (LongPoll API, avatars, attachments and etc.)
    public final static int ACCOUNT_AVATAR                    = 2000;
    public final static int NEWSFEED_ATTACHMENTS              = 2001;
    public final static int WALL_ATTACHMENTS                  = 2002;
    public final static int WALL_AVATARS                      = 2003;
    public final static int NEWSFEED_AVATARS                  = 2004;
    public final static int PROFILE_AVATARS                   = 2005;
    public final static int GROUP_AVATARS                     = 2006;
    public final static int GROUP_AVATARS_ALT                 = 2007;
    public final static int FRIEND_AVATARS                    = 2008;
    public final static int COMMENT_AVATARS                   = 2009;
    public final static int COMMENT_PHOTOS                    = 2010;
    public final static int COMMENT_VIDEO_THUMBNAILS          = 2011;
    public final static int ALBUM_PHOTOS                      = 2012;
    public final static int PHOTO_ALBUM_THUMBNAILS            = 2013;
    public final static int CONVERSATIONS_AVATARS             = 2014;
    public final static int LONGPOLL                          = 2015;
    public final static int ORIGINAL_PHOTO                    = 2016;
    public final static int VIDEO_THUMBNAILS                  = 2017;
    public final static int PARSE_JSON                        = 2018;
    public final static int UPLOAD_PROGRESS                   = 2019;
    public final static int UPLOADED_SUCCESSFULLY             = 2020;
    public final static int AUDIOS_CACHE                      = 2021;

    // Errors
    public final static int NO_INTERNET_CONNECTION            =   -1;
    public final static int CONNECTION_TIMEOUT                =   -2;
    public final static int INVALID_JSON_RESPONSE             =   -3;
    public final static int INVALID_USAGE                     =   -4;
    public final static int INVALID_TOKEN                     =   -5;
    public final static int CHAT_DISABLED                     =   -6;
    public final static int METHOD_NOT_FOUND                  =   -7;
    public final static int BANNED_ACCOUNT                    =   -8;
    public final static int ACCESS_DENIED                     =   -9;
    public final static int ACCESS_DENIED_MARSHMALLOW         =   -10;
    public final static int BROKEN_SSL_CONNECTION             =   -11;
    public final static int INTERNAL_ERROR                    =   -12;
    public final static int INSTANCE_UNAVAILABLE              =   -13;
    public final static int NOT_OPENVK_INSTANCE               =   -14;
    public final static int UNKNOWN_ERROR                     =   -15;
    public final static int UPLOAD_ERROR                      =   -16;
}
