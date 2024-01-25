0 rem imageviewer
5 gosub 1000:gosub 56500:gosub 62000:gosub 45800
10 gosub 57000
30 gosub 44000:gosub 52000
60 goto 60000

1000 rem setup screen
1010 print chr$(147);chr$(14);chr$(8);
1020 poke 53280,6:poke53281,6:poke 646,1
1030 return

39000 rem low/highbyte. Value in tb, result in lb% and hb%
39010 lb%=tb and 255:hb%=int(tb/256):return

41500 rem send and receive data
41510 poke 171,tt%:sys us,bu
41520 if peek(171)=0 then 56000
41530 poke 171,tt%:sys ug,bu+200
41540 if peek(171)=0 then 56000
41550 gosub 46000:if le% then 56200
41560 er%=0:br%=peek(169)+256*peek(170):return

42000 rem grab reply
42010 mg$="":if br%=0 then return
42020 for i=bu+200+of to bu+of+199+br%
42030 dd%=peek(i)
42040 gosub 47300
42060 mg$=mg$+chr$(dd%)
42070 next
42080 if len(mg$)<=5 then return
42090 a$=left$(mg$,5):if a$="error" or a$="Error" or a$="ERROR" then 43000
42100 return

43000 rem fatal error
43010 print "error":print:print mg$:goto 60000

44000 rem download file
44010 print "Downloading image...";
44020 tl=0:pt%=0:dc%=0
44025 gosub 44500
44030 gosub 46500:gosub 41500
44050 gosub 45000
44060 return

44500 rem contruct download url
44510 pt$=str$(pt%):ur$=gu$+"ImageViewer?file="+iu$
44530 return

45000 rem check for server error
45010 va=peek(bu+200)+256*peek(bu+201)
45020 if va<>0 then return
45030 of=2:br%=br%-2:gosub 42000
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
46020 if le% then lv%=peek(171):sys ui: sys ur
46030 return

46500 rem store url in memory
46510 lm=len(ur$):ls=lm-4:for t=1 to lm
46520 b3=bu+3:dd%=asc(mid$(ur$,t,1)):if dc%>0 then if t>=ls then 46560
46540 gosub 47300
46560 poke b3+t,dd%
46570 next:gosub 47000
46575 if dc%=0 then return
46580 t=bu+2+t:for i=0 to dc%-1:da$=dt$(i)
46590 for p=1 to len(da$):dd%=asc(mid$(da$,p,1))
46600 poke t+p,dd%:next
46610 t=t+p-1:next
46620 return

47000 rem store length in memory
47010 d=len(ur$)+4+tl:s=bu+1:gosub 47100
47020 return

47100 rem store d in s (lo/hi)
47105 tb=d:gosub 39000
47110 poke s,lb%:poke s+1,hb%:return

47300 rem convert ascii-petscii
47310 if dd%>=65 then if dd%<=90 then dd%=dd%+32:return
47320 if dd%>=97 then if dd%<=122 then dd%=dd%-32
47330 if dd%>=193 then if dd%<=218 then dd%=dd%-128
47340 return

52000 rem display loaded image
52010 poke 56576,(peek(56576) and 252) or 2
52020 ol%=peek(53272):poke 53272,120
52030 poke 53270,216
52035 poke 53265,peek(53265) or 32
52040 poke 53280,peek(34576)
52050 poke 53281,peek(34576)
52060 rem [ ldx #201 ]
52065 rem [loopy; lda 32575,x; sta 23551,x]
52070 rem [lda 32775,x; sta 23751,x; lda 32975,x; sta 23951,x]
52075 rem [lda 33175,x; sta 24151,x; lda 33375,x; sta 24351,x]
52080 rem [lda 33575,x; sta 55295,x; lda 33775,x; sta 55495,x]
52085 rem [lda 33975,x; sta 55695,x; lda 34175,x; sta 55895,x]
52090 rem [lda 34375,x; sta 56095,x; dex; bne loopy]
52095 get a$:if a$="" then 52095
52100 poke 56576,(peek(56576) and 252) or 3
52110 poke 53272,ol%
52130 poke 53265,peek(53265) and 223
52140 poke 53270,peek(53270) and 239
52150 poke 646,1:print chr$(147);:return

55000 rem init wic64
55005 print chr$(147);"Initializing wic64...";
55010 sys ui: rem init
55020 sys uc: rem check presence
55030 gosub 56000
55035 poke bu,87:poke bu+3,15: rem "w" mode, http get
55040 print "ok"
55050 return

56000 rem wic64 error?
56010 if peek(171)<>0 then return
56030 print:print "Communication error!":print
56040 print "Either there's no wic64 present"
56050 print "or the connection has timed out!"
56060 goto 60000

56200 rem load error
56230 print:print "load error (";lv%;")!"
56260 goto 60000

56500 rem check for api presence...
56505 dn%=peek(186):if dn%<8 then dn%=8
56510 lf%=peek(49152)=76 and peek(49153)=30 and peek(49154)=192
56520 if lf%=0 then print chr$(147);"Loading...":load "universal",dn%,1
56530 return

57000 rem select image to load
57010 print chr$(147);"Image URL: ";:gosub 58000:iu$=b$
57015 if iu$="" then 57010
57020 return

58000 rem input routine
58010 b$=""
58015 print chr$(164);
58020 get a$:if a$="" then 58020
58021 a%=asc(a$)
58022 if (a%=20 or a%=157) then if b$<>"" then b$=left$(b$, len(b$)-1):print chr$(20);chr$(20);:goto 58015
58030 if a%<32 and a%<>13 then 58020
58032 if (a%>127 and a%<193) then 58020
58034 if a%>218 then 58020
58060 print chr$(20);
58070 b$=b$+a$:print a$;:if len(b$)<160 and a%<>13 then 58015
58080 return

60000 rem end program
60010 poke 45,0:poke 46,10:poke 47,0:poke 48,10:poke 49,0:poke 50,10
60020 poke 55,0:poke 56,160:poke 51,0:poke 52,160:print chr$(9);:end

62000 rem init
62010 ll$=chr$(0):i=rnd(0)
62020 tt%=64:bu=24374:ui=49152
62030 ur=49155:us=49152+18:ug=49152+21
62040 uc=49152+24:sa=-1:hs=-1:he=-1:cl%=-1:xm%=0
62050 dim dt$(40)
62060 gu$=""
62065 rem gu$="http://192.168.178.20:8080/ImageViewer/"
62070 of$="test"
62100 gosub 55000:return