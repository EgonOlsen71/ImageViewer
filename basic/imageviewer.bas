0 rem Remote Image Viewer / EgonOlsen71 / 2024
2 tv%=255:tt%=64:bu=24374:ui=49152:ur=49155:us=49152+18:ug=49152+21:uc=49152+24
3 ll$=chr$(0)
5 gosub 1000:gosub 56500:gosub 62000:gosub 45800
10 gosub 57000
20 gosub 30000:if iu$="" then 10
30 er%=0:gosub 44000:if er%=1 and dr%=0 then 10
32 if er%=2 then 30
35 gosub 52000
40 gosub 20000
60 goto 20

1000 rem setup screen
1010 print chr$(147);chr$(14);chr$(8);
1020 poke 53280,6:poke53281,6:poke 646,1
1030 return

2000 rem init screen defaults
2010 gosub 1000
2015 print "{light red}Remote {green}Image {light blue}Viewer - {white}EgonOlsen71/2024{2*down}"
2020 return

10000 rem press any key
10010 print "{down}Press any key":poke 198,0
10020 get a$:if a$="" then 10020
10030 return

20000 rem save menu
20010 gosub 2000
20020 print "F1 - Show loaded image"
20025 print "F3 - Show directory"
20035 print "+- - Change target drive:";dn%
20036 print "F5 - Koala style file names: ";bv$(ks%)
20040 print "F8 - Back to load menu"
20045 print "{down}F7/RETURN - Save image"
20050 get a$:if a$="" then 20050
20060 a%=asc(a$):if a%=133 then gosub 52000:goto 20000
20070 if a%=136 or a%=13 then gosub 21000:goto 20000
20080 if a%=140 or a%=95 then return
20082 if a%=138 or a%=43 then da%=1:gosub 22000:goto 20000
20083 if a%=45 then da%=-1:gosub 22000:goto 20000
20085 if a%=88 then 60000
20086 if a%=73 then gosub 23000:goto 20000
20088 if a%=134 then gosub 59000:goto 20000
20090 if a%=135 then ks%=(ks%+1) and 1:goto 20000
20100 goto 20050

21000 rem save image to disk
21010 print:print "{down}Target file name: ";:gosub 58000
21020 n$=b$:if n$="" then return
21025 if ks%=0 then 21075
21030 n$="{129}pic a "+left$(b$+"{8*space}", 8)
21075 print chr$(147);"Saving file: ";chr$(34);n$;chr$(34)
21080 open 15,dn%,15,"s0:"+n$: close 15:t$=n$
21090 for i=1 to len(t$):poke 831+i,asc(mid$(t$,i,1)):next
21100 poke 782,3: poke 781,64:poke 780,len(t$):sys 65469:poke 780,1
21110 poke 781,dn%:poke 782,1:sys 65466
21120 poke 254,96:poke 253,0:poke 780,253:poke 782,135
21130 poke 781,17:sys 65496
21140 if (peek(783) and  1) or (st and  191) then gosub 21500
21150 return

21500 rem disk error
21510 print "{down}{down}Disk error: ";st
21520 gosub 10000
21530 return

22000 rem change target drive
22010 dn%=dn%+da%:if dn%=12 then dn%=8
22015 if dn%=7 then dn%=11
22020 return

23000 rem print system info
23010 print "{down}C'ed with MOSpeed,";fre(0);"bytes free!"
23020 gosub 10000:return

30000 rem configure imageviewer
30010 gosub 2000
30015 print "Image URL: ";:poke 646,15:print iu$;"{down}":poke 646,1
30020 print "F1 - Dithering: ";ds$(ds%);"%"
30030 print "F3 - Keep aspect ratio: ";bv$(ar%)
30045 print "F8 - Select new image{down}"
30046 print "F7/RETURN - Load image"
30050 get a$:if a$="" then 30050
30060 a%=asc(a$):if a%=133 then gosub 31000:goto 30000
30070 if a%=134 then gosub 31500:goto 30000
30080 if a%=136 or a%=13 then return
30085 if a%=88 then 60000
30088 if a%=73 then gosub 23000:goto 30000
30090 if a%=140 or a%=95 then iu$="":return
30100 goto 30050

31000 rem adjust dithering
31010 ds%=ds%+1:if ds%=5 then ds%=0
31020 return

31500 rem adjust aspect ratio
31510 ar%=(ar%+1) and 1:return

39000 rem low/highbyte. Value in tb, result in lb% and hb%
39010 lb%=tb and 255:hb%=int(tb/256):return

39500 rem clear file list
39510 for i=0 to 30:pu$(i)="":next:return

40000 rem extract image list from ram
40005 gosub 39500:pu%=0:dr%=0
40010 mp=bu+202:of=3:poke 646,7:print chr$(147);"Select image file:"
40020 br%=peek(mp):if br%=0 or pu%>21 then gosub 40500:return
40030 gosub 42000:ke$=str$(pu%)
40040 poke 646,7:print right$(ke$,len(ke$)-1);"- ";
40050 poke 646,1: gosub 40400:br%=br%+1
40055 of=of+br%:mp=mp+br%:pu$(pu%)=mg$:pu%=pu%+1
40060 goto 40020

40400 rem print file name
40410 mm$=mg$:if len(mm$)<=32 then 40450
40415 co%=8:if mid$(mm$,5,1)="s" then co%=9
40420 mm$=mid$(mm$,co%,7)+"..."+right$(mm$,25)
40450 print mm$:return

40500 rem select image file
40505 if pu%=0 then return
40510 print "{down}Enter image number: ";:gosub 58000
40520 iu%=val(b$):if iu%<0 then iu%=0
40530 if iu%>=pu% then iu%=pu%-1
40540 iu$=pu$(iu%):er%=2:return

40800 rem set timeout
40805 if tv%=0 then return
40810 poke bu,87:poke bu+1,5:poke bu+2,0
40820 poke bu+3,50:poke bu+4,tv%
40830 gosub 41800
40840 return

41500 rem send and receive data
41510 gosub 55100
41520 gosub 41800
41550 gosub 46000:if le% then gosub 56200
41560 br%=peek(169)+256*peek(170):return

41800 rem send basic request
41810 poke 171,tt%:sys us,bu
41820 gosub 56000
41830 poke 171,tt%:sys ug,bu+200
41840 gosub 56000:return

42000 rem grab reply
42010 mg$="":if br%=0 then return
42015 be=bu+of+199+br%
42020 if be>32767 then print "OOM":end
42022 rem fast path for buffer addr fitting into integer var
42025 i%=bu+200+of:be%=be:dd%=0:mp$=""
42026 rem add new string parts in chunks, which is faster (less copying involved...)
42030 rem [lda i%!; ldx i%!+1; sta loadindexxx+1; stx loadindexxx+2; loadindexxx ;ldx $ffff; lda 40000,x; sta dd%!]
42035 mp$=mp$+chr$(dd%)
42040 if len(mp$)>10 then mg$=mg$+mp$:mp$=""
42050 i%=i%+1:if i%<=be% then 42030
42060 if len(mp$)>0 then mg$=mg$+mp$
42080 if len(mg$)<=5 then return
42090 a$=left$(mg$,5):if a$="error" or a$="Error" or a$="ERROR" then 43000
42100 return

43000 rem fatal error
43010 print:print mg$
43020 gosub 10000
43025 if pu%>1 then if dr%=0 then gosub 43500
43030 er%=1:return

43500 rem set direction in case of error
43510 dr%=1
43520 if iu%=pu%-1 then dr%=-1
43530 return

44000 rem download file
44005 print chr$(147);"Loading image...";
44006 if pu%>1 then poke 646,7:print:print "{down}Press CRSR left/right to switch image...":poke 646,1
44025 gosub 44500
44030 gosub 46500:gosub 41500:if iu$="" then er%=1:return
44050 gosub 45000
44060 return

44500 rem contruct download url
44510 ur$=gu$+"ImageViewer?file="+iu$+"&dither="+ds$(ds%)
44515 if len(ur$)<245 then if ar% then ur$=ur$+"&ar=true"
44530 return

45000 rem check for server error
45010 va%=peek(bu+200)+256*peek(bu+201)
45020 if va%<>0 then if va%>257 then dr%=0:return
45030 if va%=0 then of=2:br%=br%-2:gosub 42000:return
45035 gosub 40000
45040 return

45700 rem create cloud url
45710 ur$="https://jpct.de/mospeed/ipgetiv.php":return

45800 rem get actual ip
45810 if len(gu$)<>0 then 45850
45820 print chr$(147);"Getting remote address...":gosub 45700
45830 gosub 46500:gosub 41500:of=0:gosub 42000
45840 gu$=mg$
45850 print:print "Address: ";gu$:return

46000 rem check load error
46010 le%=(peek(169)=2) and (peek(bu+200)=33) and (peek(bu+201)=48)
46020 sys ui: sys ur
46030 return

46500 rem store url in memory
46505 gosub 40800
46510 lm=len(ur$):b3=bu+3:for t=1 to lm
46520 dd%=asc(mid$(ur$,t,1))
46540 gosub 47300
46550 if dd%=32 then dd%=43
46560 poke b3+t,dd%
46570 next:gosub 47000
46575 return

47000 rem store length in memory
47010 d=len(ur$)+4:s=bu+1:gosub 47100
47020 return

47100 rem store d in s (lo/hi)
47105 tb=d:gosub 39000
47110 poke s,lb%:poke s+1,hb%:return

47300 rem convert ascii-petscii
47310 if dd%>=97 then if dd%<=122 then dd%=dd%-32:return
47320 if dd%>=65 then if dd%<=90 then dd%=dd%+32:return
47330 if dd%>=193 then if dd%<=218 then dd%=dd%-128:return
47335 if dd%=95 then dd%=164:return
47336 if dd%=164 then dd%=95:return
47340 return

52000 rem display loaded image
52005 if dr%<>0 then a%=157:if dr%=1 then a%=29
52006 if dr%<>0 then 52152
52010 poke 198,0:poke 56576,(peek(56576) and 252) or 2
52020 ol%=peek(53272):poke 53272,120
52030 poke 53270,216
52035 poke 53265,peek(53265) or 32
52040 poke 53280,peek(34576)
52050 poke 53281,peek(34576)
52060 rem [ ldx #251 ]
52065 rem [loopy; lda 32575,x; sta 23551,x]
52070 rem [lda 32825,x; sta 23801,x; lda 33075,x; sta 24051,x]
52075 rem [lda 33325,x; sta 24301,x;]
52080 rem [lda 33575,x; sta 55295,x; lda 33825,x; sta 55545,x]
52085 rem [lda 34075,x; sta 55795,x; lda 34325,x; sta 56045,x]
52090 rem [dex; bne loopy]
52095 get a$:if a$="" then 52095
52096 a%=asc(a$)
52100 poke 56576,(peek(56576) and 252) or 3
52110 poke 53272,ol%
52130 poke 53265,peek(53265) and 223
52140 poke 53270,peek(53270) and 239
52150 poke 646,1:print chr$(147);
52152 if pu%>0 then if a%=29 or a%=157 then gosub 52160:goto 52500
52154 return
52160 rem switch image
52170 dr%=1:if a%=157 then dr%=-1:iu%=iu%-1:goto 52180
52175 iu%=iu%+1
52180 if iu%<0 then iu%=0:gosub 53000:return
52190 if iu%=pu% then iu%=pu%-1:gosub 53000:return
52200 iu$=pu$(iu%):er%=0:gosub 1000:gosub 44000:return
52500 rem next image if error
52510 if (er%=1 and dr%<>0) or er%=0 then 52000: rem error and 'next image' or no error.
52520 return

53000 rem flash screen
53010 tx=ti:oc%=peek(53280)
53020 poke 53280,(peek(53280)+1) and 3
53030 if ti-tx<30 then 53020
53040 poke 53280,oc%:dr%=0:return

55000 rem init wic64
55010 sys ui: rem init
55020 sys uc: rem check presence
55030 gosub 56000
55100 poke bu,87:poke bu+3,15: rem "w" mode, http get
55110 return

56000 rem wic64 error?
56010 if peek(171)<>0 then return
56030 print:print "Communication error!":print
56040 print "Check your WiC64 and its firmware!"
56060 goto 60000

56200 rem load error
56230 print:print "Load error! Invalid URL?"
56260 gosub 10000
56270 iu$="":er%=1:return

56500 rem check for api presence...
56505 dn%=peek(186):if dn%<8 then dn%=8
56510 lf%=peek(49152)=76 and peek(49153)=30 and peek(49154)=192
56520 if lf%=0 then print chr$(147);"Com. routines are missing!":goto 60000
56530 return

57000 rem select image to load
57010 gosub 2000:poke 646, 15:gosub 39500:pu%=0
57020 print "Enter img/pdf URL. You can omit https://";
57022 print "{down}If you enter a page URL instead of an"
57024 print "image URL, you can choose between the"
57026 print "first 22 unique images on that page."
57027 print "If you enter a single word or sentence,"
57028 print "the first 10 results of an image search"
57029 print "will be returned."
57037 print "{down}Type {95} for underscore, RETURN to"
57038 print "repeat the last URL and x to exit"
57040 print "the program!{2*down}"
57050 poke 646,1:print "Image URL: ";:gosub 58000:iu$=b$
57060 if iu$="" then 57500
57065 ou$ = iu$
57068 if iu$="x" then 60000
57070 return

57500 rem fill url with old one, if present
57510 if ou$="" then goto 57000
57520 iu$=ou$:print iu$
57530 return

58000 rem input routine
58010 b$=""
58015 print chr$(164);
58020 get a$:if a$="" then 58020
58021 a%=asc(a$)
58022 if (a%=20 or a%=157) then if b$<>"" then b$=left$(b$, len(b$)-1):print chr$(20);chr$(20);:goto 58015
58030 if a%<32 then if a%<>13 then 58020
58032 if a%>127 then if a%<193 then 58020
58034 if a%>218 then 58020
58060 print chr$(20);:if a%=13 then a$=""
58065 if a%=95 then a$=chr$(164)
58070 b$=b$+a$:print a$;:if len(b$)<160 then if a%<>13 then 58015
58080 return

59000 rem print directory
59010 print chr$(147);
59020 open 1,dn%,0,"$":poke 781,1:sys 65478:get a$,a$
59030 get a$,a$,h$,l$:if st then sys 65484:close 1:goto 59070
59040 print asc(h$+ll$)+256*asc(l$+ll$);
59050 get a$,b$:if b$ then print a$b$;:if st=0 then 59050
59060 print a$:goto 59030
59070 get a$:if a$="" then 59070
59080 return

60000 rem end program
60010 poke 45,0:poke 46,10:poke 47,0:poke 48,10:poke 49,0:poke 50,10
60020 poke 55,0:poke 56,160:poke 51,0:poke 52,160:print chr$(9);
60030 print:print "{down}Have a nice BASIC!":end

62000 rem init
62050 dim pu$(22):ou$="":ks%=1:ds%=1:ar%=1
62060 gu$=""
62065 dim bv$(1):bv$(0)="no":bv$(1)="yes"
62070 dim ds$(4):ds$(0)="100":ds$(1)="50":ds$(2)="25":ds$(3)="10":ds$(4)="0"
62080 dd%=0:i%=0:pu%=0:dr%=0: rem setup conversion table
62081 rem [ldx i%!; stx dd%!]
62082 gosub 47300:rem [ldx i%!; lda dd%!; sta 40000,x]
62085 rem [inc i%!; bne 62081!]
62100 gosub 55000:return