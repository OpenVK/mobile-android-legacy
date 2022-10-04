package uk.openvk.android.legacy.layouts;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ViewGroup;
import uk.openvk.android.legacy.Global;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class FlowLayout extends ViewGroup {
  private Vector<Integer> lineHeights = new Vector<Integer>();
  
  private List<LayoutParams> lparams;
  
  private int measuredHeight = 0;
  
  public int pwidth = Global.scale(5.0F);
  
  static {
    boolean bool;
    if (!FlowLayout.class.desiredAssertionStatus()) {
      bool = true;
    } else {
      bool = false;
    }
      boolean assertionsDisabled = bool;
  }
  
  public FlowLayout(Context paramContext) {
    super(paramContext);
  }
  
  public FlowLayout(Context paramContext, AttributeSet paramAttributeSet) {
    super(paramContext, paramAttributeSet);
  }
  
  protected boolean checkLayoutParams(ViewGroup.LayoutParams paramLayoutParams) {
    return (paramLayoutParams instanceof LayoutParams);
  }
  
  protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
    return new LayoutParams(Global.scale(2.0F), Global.scale(2.0F));
  }
  
  public int getFullHeight() {
    int i = 0;
    Iterator<Integer> iterator = this.lineHeights.iterator();
    while (true) {
      if (!iterator.hasNext())
        return i; 
      i += ((Integer)iterator.next()).intValue();
    } 
  }
  
  public void layoutWithParams(List<LayoutParams> paramList, int paramInt1, int paramInt2) {
    // Byte code:
    //   0: aload_0
    //   1: aload_1
    //   2: putfield lparams : Ljava/util/List;
    //   5: new java/util/ArrayList
    //   8: dup
    //   9: invokespecial <init> : ()V
    //   12: astore #4
    //   14: aload_1
    //   15: invokeinterface size : ()I
    //   20: istore #5
    //   22: aload_0
    //   23: invokevirtual getPaddingLeft : ()I
    //   26: istore #6
    //   28: aload_0
    //   29: invokevirtual getPaddingTop : ()I
    //   32: istore #7
    //   34: iconst_0
    //   35: istore #8
    //   37: aload_0
    //   38: getfield lineHeights : Ljava/util/Vector;
    //   41: invokevirtual clear : ()V
    //   44: iconst_0
    //   45: istore #9
    //   47: iconst_0
    //   48: istore #10
    //   50: iconst_0
    //   51: istore_3
    //   52: iconst_0
    //   53: istore #11
    //   55: iload #11
    //   57: iload #5
    //   59: if_icmplt -> 124
    //   62: iload #10
    //   64: ifle -> 80
    //   67: aload_0
    //   68: getfield lineHeights : Ljava/util/Vector;
    //   71: iload #10
    //   73: invokestatic valueOf : (I)Ljava/lang/Integer;
    //   76: invokevirtual add : (Ljava/lang/Object;)Z
    //   79: pop
    //   80: aload_0
    //   81: invokevirtual getPaddingLeft : ()I
    //   84: istore #9
    //   86: aload_0
    //   87: invokevirtual getPaddingTop : ()I
    //   90: istore #12
    //   92: iconst_0
    //   93: istore_3
    //   94: iconst_0
    //   95: istore #10
    //   97: iconst_0
    //   98: istore #8
    //   100: iconst_0
    //   101: istore #13
    //   103: iconst_0
    //   104: istore #6
    //   106: iload #6
    //   108: iload #5
    //   110: if_icmplt -> 369
    //   113: aload_0
    //   114: aload_0
    //   115: invokevirtual getFullHeight : ()I
    //   118: putfield measuredHeight : I
    //   121: aload #4
    //   123: areturn
    //   124: aload_1
    //   125: iload #11
    //   127: invokeinterface get : (I)Ljava/lang/Object;
    //   132: checkcast com/vkontakte/android/ui/FlowLayout$LayoutParams
    //   135: astore #14
    //   137: aload #14
    //   139: getfield width : I
    //   142: ifgt -> 170
    //   145: iload_2
    //   146: istore #13
    //   148: aload #14
    //   150: getfield height : I
    //   153: istore #15
    //   155: iload #15
    //   157: ifge -> 180
    //   160: new java/lang/IllegalArgumentException
    //   163: dup
    //   164: ldc 'Height should be constant'
    //   166: invokespecial <init> : (Ljava/lang/String;)V
    //   169: athrow
    //   170: aload #14
    //   172: getfield width : I
    //   175: istore #13
    //   177: goto -> 148
    //   180: iload #8
    //   182: ifne -> 215
    //   185: iload #9
    //   187: istore #16
    //   189: iload #10
    //   191: istore #17
    //   193: iload #6
    //   195: istore #18
    //   197: iload #7
    //   199: istore #12
    //   201: iload #6
    //   203: iload #13
    //   205: iadd
    //   206: aload_0
    //   207: getfield pwidth : I
    //   210: iload_2
    //   211: iadd
    //   212: if_icmple -> 257
    //   215: aload_0
    //   216: invokevirtual getPaddingLeft : ()I
    //   219: istore #18
    //   221: iload #7
    //   223: iload #10
    //   225: iload #9
    //   227: invokestatic max : (II)I
    //   230: iadd
    //   231: istore #12
    //   233: aload_0
    //   234: getfield lineHeights : Ljava/util/Vector;
    //   237: iload #10
    //   239: iload #9
    //   241: invokestatic max : (II)I
    //   244: invokestatic valueOf : (I)Ljava/lang/Integer;
    //   247: invokevirtual add : (Ljava/lang/Object;)Z
    //   250: pop
    //   251: iconst_0
    //   252: istore #17
    //   254: iconst_0
    //   255: istore #16
    //   257: iload #17
    //   259: aload #14
    //   261: getfield vertical_spacing : I
    //   264: iload #15
    //   266: iadd
    //   267: invokestatic max : (II)I
    //   270: istore #10
    //   272: aload #14
    //   274: getfield floating : Z
    //   277: ifeq -> 350
    //   280: iload #12
    //   282: aload #14
    //   284: getfield vertical_spacing : I
    //   287: iload #15
    //   289: iadd
    //   290: iadd
    //   291: istore #12
    //   293: iload #16
    //   295: aload #14
    //   297: getfield vertical_spacing : I
    //   300: iload #15
    //   302: iadd
    //   303: iadd
    //   304: istore #9
    //   306: iload_3
    //   307: iload #18
    //   309: iload #13
    //   311: iadd
    //   312: invokestatic max : (II)I
    //   315: istore_3
    //   316: aload #14
    //   318: getfield breakAfter : Z
    //   321: istore #8
    //   323: iload_3
    //   324: iload #18
    //   326: aload #14
    //   328: getfield horizontal_spacing : I
    //   331: isub
    //   332: invokestatic max : (II)I
    //   335: istore_3
    //   336: iinc #11, 1
    //   339: iload #18
    //   341: istore #6
    //   343: iload #12
    //   345: istore #7
    //   347: goto -> 55
    //   350: iconst_0
    //   351: istore #9
    //   353: iload #18
    //   355: aload #14
    //   357: getfield horizontal_spacing : I
    //   360: iload #13
    //   362: iadd
    //   363: iadd
    //   364: istore #18
    //   366: goto -> 316
    //   369: aload_1
    //   370: iload #6
    //   372: invokeinterface get : (I)Ljava/lang/Object;
    //   377: checkcast com/vkontakte/android/ui/FlowLayout$LayoutParams
    //   380: astore #14
    //   382: aload #14
    //   384: getfield width : I
    //   387: ifgt -> 415
    //   390: iload_2
    //   391: istore #16
    //   393: aload #14
    //   395: getfield height : I
    //   398: istore #17
    //   400: iload #17
    //   402: ifge -> 425
    //   405: new java/lang/IllegalArgumentException
    //   408: dup
    //   409: ldc 'Height should be constant'
    //   411: invokespecial <init> : (Ljava/lang/String;)V
    //   414: athrow
    //   415: aload #14
    //   417: getfield width : I
    //   420: istore #16
    //   422: goto -> 393
    //   425: iload #12
    //   427: istore #18
    //   429: aload #14
    //   431: getfield floating : Z
    //   434: ifne -> 449
    //   437: iload #12
    //   439: istore #18
    //   441: iload #10
    //   443: ifeq -> 449
    //   446: iload_3
    //   447: istore #18
    //   449: iload #8
    //   451: ifne -> 480
    //   454: iload #13
    //   456: istore #11
    //   458: iload #9
    //   460: istore #7
    //   462: iload #18
    //   464: istore #12
    //   466: iload #9
    //   468: iload #16
    //   470: iadd
    //   471: aload_0
    //   472: getfield pwidth : I
    //   475: iload_2
    //   476: iadd
    //   477: if_icmple -> 512
    //   480: aload_0
    //   481: invokevirtual getPaddingLeft : ()I
    //   484: istore #7
    //   486: iload #18
    //   488: aload_0
    //   489: getfield lineHeights : Ljava/util/Vector;
    //   492: iload #13
    //   494: invokevirtual elementAt : (I)Ljava/lang/Object;
    //   497: checkcast java/lang/Integer
    //   500: invokevirtual intValue : ()I
    //   503: iadd
    //   504: istore #12
    //   506: iload #13
    //   508: iconst_1
    //   509: iadd
    //   510: istore #11
    //   512: iload #7
    //   514: istore #9
    //   516: aload #14
    //   518: getfield center : Z
    //   521: ifeq -> 537
    //   524: aload_0
    //   525: invokevirtual getWidth : ()I
    //   528: iconst_2
    //   529: idiv
    //   530: iload #16
    //   532: iconst_2
    //   533: idiv
    //   534: isub
    //   535: istore #9
    //   537: ldc 'vk'
    //   539: new java/lang/StringBuilder
    //   542: dup
    //   543: iload #9
    //   545: invokestatic valueOf : (I)Ljava/lang/String;
    //   548: invokespecial <init> : (Ljava/lang/String;)V
    //   551: ldc ';'
    //   553: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   556: iload #12
    //   558: invokevirtual append : (I)Ljava/lang/StringBuilder;
    //   561: ldc ';'
    //   563: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   566: iload #16
    //   568: invokevirtual append : (I)Ljava/lang/StringBuilder;
    //   571: ldc ';'
    //   573: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   576: iload #17
    //   578: invokevirtual append : (I)Ljava/lang/StringBuilder;
    //   581: invokevirtual toString : ()Ljava/lang/String;
    //   584: invokestatic v : (Ljava/lang/String;Ljava/lang/String;)V
    //   587: aload #4
    //   589: new android/graphics/Rect
    //   592: dup
    //   593: iload #9
    //   595: iload #12
    //   597: iload #9
    //   599: iload #16
    //   601: iadd
    //   602: iload #12
    //   604: iload #17
    //   606: iadd
    //   607: invokespecial <init> : (IIII)V
    //   610: invokevirtual add : (Ljava/lang/Object;)Z
    //   613: pop
    //   614: aload #14
    //   616: getfield floating : Z
    //   619: ifeq -> 675
    //   622: iload_3
    //   623: istore #18
    //   625: iload #10
    //   627: istore_3
    //   628: iload #10
    //   630: ifne -> 639
    //   633: iload #12
    //   635: istore #18
    //   637: iconst_1
    //   638: istore_3
    //   639: iload #12
    //   641: aload #14
    //   643: getfield vertical_spacing : I
    //   646: iload #17
    //   648: iadd
    //   649: iadd
    //   650: istore #12
    //   652: iload_3
    //   653: istore #10
    //   655: iload #18
    //   657: istore_3
    //   658: aload #14
    //   660: getfield breakAfter : Z
    //   663: istore #8
    //   665: iinc #6, 1
    //   668: iload #11
    //   670: istore #13
    //   672: goto -> 106
    //   675: iconst_0
    //   676: istore #10
    //   678: iload #9
    //   680: aload #14
    //   682: getfield horizontal_spacing : I
    //   685: iload #16
    //   687: iadd
    //   688: iadd
    //   689: istore #9
    //   691: goto -> 658
  }
  
  protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
    // Byte code:
    //   0: aload_0
    //   1: invokevirtual getChildCount : ()I
    //   4: istore #6
    //   6: aload_0
    //   7: invokevirtual getPaddingLeft : ()I
    //   10: istore #7
    //   12: aload_0
    //   13: invokevirtual getPaddingTop : ()I
    //   16: istore #5
    //   18: iconst_0
    //   19: istore #8
    //   21: iconst_0
    //   22: istore_1
    //   23: iconst_0
    //   24: istore #9
    //   26: iconst_0
    //   27: istore_3
    //   28: iconst_0
    //   29: istore #10
    //   31: iload #10
    //   33: iload #6
    //   35: if_icmplt -> 39
    //   38: return
    //   39: aload_0
    //   40: iload #10
    //   42: invokevirtual getChildAt : (I)Landroid/view/View;
    //   45: astore #11
    //   47: iload_1
    //   48: istore #12
    //   50: iload_3
    //   51: istore #13
    //   53: iload #8
    //   55: istore #14
    //   57: iload #9
    //   59: istore #15
    //   61: iload #7
    //   63: istore #16
    //   65: iload #5
    //   67: istore #17
    //   69: aload #11
    //   71: invokevirtual getVisibility : ()I
    //   74: bipush #8
    //   76: if_icmpeq -> 318
    //   79: aload #11
    //   81: invokevirtual getLayoutParams : ()Landroid/view/ViewGroup$LayoutParams;
    //   84: checkcast com/vkontakte/android/ui/FlowLayout$LayoutParams
    //   87: astore #18
    //   89: aload #18
    //   91: getfield width : I
    //   94: ifgt -> 346
    //   97: aload #11
    //   99: invokevirtual getMeasuredWidth : ()I
    //   102: istore #13
    //   104: aload #18
    //   106: getfield height : I
    //   109: ifgt -> 356
    //   112: aload #11
    //   114: invokevirtual getMeasuredHeight : ()I
    //   117: istore #15
    //   119: iload #5
    //   121: istore #17
    //   123: aload #18
    //   125: getfield floating : Z
    //   128: ifne -> 143
    //   131: iload #5
    //   133: istore #17
    //   135: iload #9
    //   137: ifeq -> 143
    //   140: iload_3
    //   141: istore #17
    //   143: iload_1
    //   144: ifne -> 176
    //   147: iload #8
    //   149: istore #14
    //   151: iload #7
    //   153: istore #16
    //   155: iload #17
    //   157: istore #5
    //   159: iload #7
    //   161: iload #13
    //   163: iadd
    //   164: aload_0
    //   165: getfield pwidth : I
    //   168: iload #4
    //   170: iload_2
    //   171: isub
    //   172: iadd
    //   173: if_icmple -> 208
    //   176: aload_0
    //   177: invokevirtual getPaddingLeft : ()I
    //   180: istore #16
    //   182: iload #17
    //   184: aload_0
    //   185: getfield lineHeights : Ljava/util/Vector;
    //   188: iload #8
    //   190: invokevirtual elementAt : (I)Ljava/lang/Object;
    //   193: checkcast java/lang/Integer
    //   196: invokevirtual intValue : ()I
    //   199: iadd
    //   200: istore #5
    //   202: iload #8
    //   204: iconst_1
    //   205: iadd
    //   206: istore #14
    //   208: iload #16
    //   210: istore #7
    //   212: aload #18
    //   214: getfield center : Z
    //   217: ifeq -> 233
    //   220: aload_0
    //   221: invokevirtual getWidth : ()I
    //   224: iconst_2
    //   225: idiv
    //   226: iload #13
    //   228: iconst_2
    //   229: idiv
    //   230: isub
    //   231: istore #7
    //   233: aload #11
    //   235: iload #7
    //   237: iload #5
    //   239: iload #7
    //   241: iload #13
    //   243: iadd
    //   244: iload #5
    //   246: iload #15
    //   248: iadd
    //   249: invokevirtual layout : (IIII)V
    //   252: aload #18
    //   254: getfield floating : Z
    //   257: ifeq -> 366
    //   260: iload_3
    //   261: istore #8
    //   263: iload #9
    //   265: istore_3
    //   266: iload #9
    //   268: ifne -> 277
    //   271: iload #5
    //   273: istore #8
    //   275: iconst_1
    //   276: istore_3
    //   277: iload #5
    //   279: aload #18
    //   281: getfield vertical_spacing : I
    //   284: iload #15
    //   286: iadd
    //   287: iadd
    //   288: istore #5
    //   290: iload_3
    //   291: istore #9
    //   293: iload #8
    //   295: istore_3
    //   296: aload #18
    //   298: getfield breakAfter : Z
    //   301: istore #12
    //   303: iload #5
    //   305: istore #17
    //   307: iload #7
    //   309: istore #16
    //   311: iload #9
    //   313: istore #15
    //   315: iload_3
    //   316: istore #13
    //   318: iinc #10, 1
    //   321: iload #12
    //   323: istore_1
    //   324: iload #13
    //   326: istore_3
    //   327: iload #14
    //   329: istore #8
    //   331: iload #15
    //   333: istore #9
    //   335: iload #16
    //   337: istore #7
    //   339: iload #17
    //   341: istore #5
    //   343: goto -> 31
    //   346: aload #18
    //   348: getfield width : I
    //   351: istore #13
    //   353: goto -> 104
    //   356: aload #18
    //   358: getfield height : I
    //   361: istore #15
    //   363: goto -> 119
    //   366: iconst_0
    //   367: istore #9
    //   369: iload #7
    //   371: aload #18
    //   373: getfield horizontal_spacing : I
    //   376: iload #13
    //   378: iadd
    //   379: iadd
    //   380: istore #7
    //   382: goto -> 296
  }
  
  protected void onMeasure(int paramInt1, int paramInt2) {
    // Byte code:
    //   0: getstatic com/vkontakte/android/ui/FlowLayout.$assertionsDisabled : Z
    //   3: ifne -> 21
    //   6: iload_1
    //   7: invokestatic getMode : (I)I
    //   10: ifne -> 21
    //   13: new java/lang/AssertionError
    //   16: dup
    //   17: invokespecial <init> : ()V
    //   20: athrow
    //   21: iload_1
    //   22: invokestatic getSize : (I)I
    //   25: aload_0
    //   26: invokevirtual getPaddingLeft : ()I
    //   29: isub
    //   30: aload_0
    //   31: invokevirtual getPaddingRight : ()I
    //   34: isub
    //   35: istore_3
    //   36: iload_2
    //   37: invokestatic getSize : (I)I
    //   40: aload_0
    //   41: invokevirtual getPaddingTop : ()I
    //   44: isub
    //   45: aload_0
    //   46: invokevirtual getPaddingBottom : ()I
    //   49: isub
    //   50: istore #4
    //   52: aload_0
    //   53: invokevirtual getChildCount : ()I
    //   56: istore #5
    //   58: iconst_0
    //   59: istore #6
    //   61: aload_0
    //   62: invokevirtual getPaddingLeft : ()I
    //   65: istore #7
    //   67: aload_0
    //   68: invokevirtual getPaddingTop : ()I
    //   71: istore #8
    //   73: iconst_0
    //   74: istore #9
    //   76: iload_2
    //   77: invokestatic getMode : (I)I
    //   80: ldc -2147483648
    //   82: if_icmpne -> 192
    //   85: iload #4
    //   87: ldc -2147483648
    //   89: invokestatic makeMeasureSpec : (II)I
    //   92: istore #10
    //   94: aload_0
    //   95: getfield lineHeights : Ljava/util/Vector;
    //   98: invokevirtual clear : ()V
    //   101: iconst_0
    //   102: istore #11
    //   104: iconst_0
    //   105: istore #12
    //   107: iconst_0
    //   108: istore #13
    //   110: aload_0
    //   111: getfield lparams : Ljava/util/List;
    //   114: ifnull -> 622
    //   117: aload_0
    //   118: getfield lparams : Ljava/util/List;
    //   121: invokeinterface size : ()I
    //   126: istore #14
    //   128: iload #13
    //   130: iload #5
    //   132: iload #14
    //   134: invokestatic max : (II)I
    //   137: if_icmplt -> 202
    //   140: iload_2
    //   141: invokestatic getMode : (I)I
    //   144: ifne -> 649
    //   147: iload #6
    //   149: iload #12
    //   151: invokestatic max : (II)I
    //   154: istore #7
    //   156: aload_0
    //   157: getfield lineHeights : Ljava/util/Vector;
    //   160: invokevirtual iterator : ()Ljava/util/Iterator;
    //   163: astore #15
    //   165: aload #15
    //   167: invokeinterface hasNext : ()Z
    //   172: ifne -> 628
    //   175: iload_1
    //   176: invokestatic getMode : (I)I
    //   179: ldc 1073741824
    //   181: if_icmpne -> 720
    //   184: aload_0
    //   185: iload_3
    //   186: iload #7
    //   188: invokevirtual setMeasuredDimension : (II)V
    //   191: return
    //   192: iconst_0
    //   193: iconst_0
    //   194: invokestatic makeMeasureSpec : (II)I
    //   197: istore #10
    //   199: goto -> 94
    //   202: aload_0
    //   203: iload #13
    //   205: invokevirtual getChildAt : (I)Landroid/view/View;
    //   208: astore #16
    //   210: aload #16
    //   212: ifnull -> 225
    //   215: aload #16
    //   217: invokevirtual getVisibility : ()I
    //   220: bipush #8
    //   222: if_icmpne -> 254
    //   225: iload #11
    //   227: istore #17
    //   229: iload #12
    //   231: istore #18
    //   233: iload #6
    //   235: istore #19
    //   237: iload #9
    //   239: istore #14
    //   241: iload #7
    //   243: istore #20
    //   245: iload #8
    //   247: istore #21
    //   249: aload #16
    //   251: ifnonnull -> 507
    //   254: aload #16
    //   256: ifnull -> 537
    //   259: aload #16
    //   261: invokevirtual getLayoutParams : ()Landroid/view/ViewGroup$LayoutParams;
    //   264: checkcast com/vkontakte/android/ui/FlowLayout$LayoutParams
    //   267: astore #15
    //   269: aload #16
    //   271: ifnull -> 299
    //   274: aload #15
    //   276: getfield width : I
    //   279: ifgt -> 556
    //   282: iload_3
    //   283: ldc -2147483648
    //   285: invokestatic makeMeasureSpec : (II)I
    //   288: istore #14
    //   290: aload #16
    //   292: iload #14
    //   294: iload #10
    //   296: invokevirtual measure : (II)V
    //   299: aload #15
    //   301: getfield width : I
    //   304: ifgt -> 577
    //   307: aload #16
    //   309: ifnull -> 571
    //   312: aload #16
    //   314: invokevirtual getMeasuredWidth : ()I
    //   317: istore #14
    //   319: aload #15
    //   321: getfield height : I
    //   324: ifgt -> 593
    //   327: aload #16
    //   329: ifnull -> 587
    //   332: aload #16
    //   334: invokevirtual getMeasuredHeight : ()I
    //   337: istore #19
    //   339: iload #11
    //   341: ifne -> 374
    //   344: iload #12
    //   346: istore #18
    //   348: iload #6
    //   350: istore #22
    //   352: iload #7
    //   354: istore #20
    //   356: iload #8
    //   358: istore #21
    //   360: iload #7
    //   362: iload #14
    //   364: iadd
    //   365: aload_0
    //   366: getfield pwidth : I
    //   369: iload_3
    //   370: iadd
    //   371: if_icmple -> 416
    //   374: aload_0
    //   375: invokevirtual getPaddingLeft : ()I
    //   378: istore #20
    //   380: iload #8
    //   382: iload #6
    //   384: iload #12
    //   386: invokestatic max : (II)I
    //   389: iadd
    //   390: istore #21
    //   392: aload_0
    //   393: getfield lineHeights : Ljava/util/Vector;
    //   396: iload #6
    //   398: iload #12
    //   400: invokestatic max : (II)I
    //   403: invokestatic valueOf : (I)Ljava/lang/Integer;
    //   406: invokevirtual add : (Ljava/lang/Object;)Z
    //   409: pop
    //   410: iconst_0
    //   411: istore #22
    //   413: iconst_0
    //   414: istore #18
    //   416: iload #22
    //   418: aload #15
    //   420: getfield vertical_spacing : I
    //   423: iload #19
    //   425: iadd
    //   426: invokestatic max : (II)I
    //   429: istore #8
    //   431: aload #15
    //   433: getfield floating : Z
    //   436: ifeq -> 603
    //   439: iload #21
    //   441: aload #15
    //   443: getfield vertical_spacing : I
    //   446: iload #19
    //   448: iadd
    //   449: iadd
    //   450: istore #21
    //   452: iload #18
    //   454: aload #15
    //   456: getfield vertical_spacing : I
    //   459: iload #19
    //   461: iadd
    //   462: iadd
    //   463: istore #6
    //   465: iload #9
    //   467: iload #20
    //   469: iload #14
    //   471: iadd
    //   472: invokestatic max : (II)I
    //   475: istore #9
    //   477: aload #15
    //   479: getfield breakAfter : Z
    //   482: istore #17
    //   484: iload #9
    //   486: iload #20
    //   488: aload #15
    //   490: getfield horizontal_spacing : I
    //   493: isub
    //   494: invokestatic max : (II)I
    //   497: istore #14
    //   499: iload #8
    //   501: istore #19
    //   503: iload #6
    //   505: istore #18
    //   507: iinc #13, 1
    //   510: iload #17
    //   512: istore #11
    //   514: iload #18
    //   516: istore #12
    //   518: iload #19
    //   520: istore #6
    //   522: iload #14
    //   524: istore #9
    //   526: iload #20
    //   528: istore #7
    //   530: iload #21
    //   532: istore #8
    //   534: goto -> 110
    //   537: aload_0
    //   538: getfield lparams : Ljava/util/List;
    //   541: iload #13
    //   543: invokeinterface get : (I)Ljava/lang/Object;
    //   548: checkcast com/vkontakte/android/ui/FlowLayout$LayoutParams
    //   551: astore #15
    //   553: goto -> 269
    //   556: aload #15
    //   558: getfield width : I
    //   561: ldc 1073741824
    //   563: invokestatic makeMeasureSpec : (II)I
    //   566: istore #14
    //   568: goto -> 290
    //   571: iload_3
    //   572: istore #14
    //   574: goto -> 319
    //   577: aload #15
    //   579: getfield width : I
    //   582: istore #14
    //   584: goto -> 319
    //   587: iconst_0
    //   588: istore #19
    //   590: goto -> 339
    //   593: aload #15
    //   595: getfield height : I
    //   598: istore #19
    //   600: goto -> 339
    //   603: iconst_0
    //   604: istore #6
    //   606: iload #20
    //   608: aload #15
    //   610: getfield horizontal_spacing : I
    //   613: iload #14
    //   615: iadd
    //   616: iadd
    //   617: istore #20
    //   619: goto -> 477
    //   622: iconst_0
    //   623: istore #14
    //   625: goto -> 128
    //   628: iload #7
    //   630: aload #15
    //   632: invokeinterface next : ()Ljava/lang/Object;
    //   637: checkcast java/lang/Integer
    //   640: invokevirtual intValue : ()I
    //   643: iadd
    //   644: istore #7
    //   646: goto -> 165
    //   649: iload #4
    //   651: istore #7
    //   653: iload_2
    //   654: invokestatic getMode : (I)I
    //   657: ldc -2147483648
    //   659: if_icmpne -> 175
    //   662: iload #4
    //   664: istore #7
    //   666: iload #8
    //   668: iload #6
    //   670: iadd
    //   671: iload #4
    //   673: if_icmpge -> 175
    //   676: iload #6
    //   678: istore_2
    //   679: aload_0
    //   680: getfield lineHeights : Ljava/util/Vector;
    //   683: invokevirtual iterator : ()Ljava/util/Iterator;
    //   686: astore #15
    //   688: iload_2
    //   689: istore #7
    //   691: aload #15
    //   693: invokeinterface hasNext : ()Z
    //   698: ifeq -> 175
    //   701: iload_2
    //   702: aload #15
    //   704: invokeinterface next : ()Ljava/lang/Object;
    //   709: checkcast java/lang/Integer
    //   712: invokevirtual intValue : ()I
    //   715: iadd
    //   716: istore_2
    //   717: goto -> 688
    //   720: aload_0
    //   721: iload #9
    //   723: iload #7
    //   725: invokevirtual setMeasuredDimension : (II)V
    //   728: goto -> 191
  }
  
  public void resetParams() {
    this.lparams = null;
  }
  
  public static class LayoutParams extends ViewGroup.LayoutParams {
    public boolean breakAfter;
    
    public boolean center;
    
    public boolean floating;
    
    public int height;
    
    public int horizontal_spacing;
    
    public int vertical_spacing;
    
    public int width;
    
    public LayoutParams() {
      super(0, 0);
    }
    
    public LayoutParams(int param1Int1, int param1Int2) {
      super(0, 0);
      this.horizontal_spacing = param1Int1;
      this.vertical_spacing = param1Int2;
    }
  }
}


/* Location:              C:\Users\Dmitry\vk.3.0.4.jar!\com\vkontakte\androi\\ui\FlowLayout.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */