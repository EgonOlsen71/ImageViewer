0 rem Local Image Viewer (Koala format) / EgonOlsen71 / 2024
5 dn%=peek(186):if dn%<8 then dn%=8
8 dim ff$(42):ll$=chr$(0)
10 if lf%<>0 then gosub 52000:gosub 1000:gosub 58000:gosub 55010
20 gosub 1000
30 gosub 3000
100 end

1000 rem setup screen
1010 print chr$(147);chr$(14);chr$(8);
1020 poke 53280,6:poke53281,6:poke 646,1
1030 return

2000 rem init screen defaults
2010 gosub 1000:print "{red}Local {green}Koala {light blue}Viewer - {white}EgonOlsen71/2024{2*down}"
2020 return

3000 rem main menu
3005 gosub 2000
3006 poke 646,15:print "This is a viewer for Koala images."
3007 print "If you want to view web images, load"
3008 print "imageviewer instead!{down}{down}":poke 646,1
3010 print "F1 - Change drive:";dn%
3020 print "F8 - Quit"
3030 print "{down}F7/RETURN - Select and show image"
3040 get a$:if a$="" then 3040
3050 a%=asc(a$)
3060 if a%=133 then gosub 5000:goto 3000
3070 if a%=140 or a%=88 then 60000
3080 if a%=13 or a%=136 then gosub 10000:goto 3000
3085 if a%=73 then gosub 23000:goto 3000
3090 goto 3040

5000 rem change target drive
5010 dn%=dn%+1:if dn%=12 then dn%=8
5020 return

10000 rem select and show image
10010 gosub 59000
10020 return

11000 rem press any key
11010 print "{down}Press any key"
11020 get a$:if a$="" then 11020
11030 return

23000 rem print system info
23010 print "{down}C'ed with MOSpeed,";fre(0);"bytes free!"
23020 gosub 11000:return

52000 rem display loaded image
52005 poke 43,lb%:poke 44,hb%
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
52150 poke 646,1:print chr$(147);
52154 return

54000 rem load file
54002 lb%=peek(43):hb%=peek(44)
54005 poke 43,0:poke 44,96
54006 rem hack, to use LOAD but with images not starting at $6000
54010 lf%=1:load ff$(cs%),dn%,0
54020 run

55000 rem select file to load
55005 cs%=0:os%=0
55010 lf%=0:gosub 56000

56000 rem highlight selection
56010 dc%=os%:gosub 56500
56020 print chr$(34);ff$(os%);chr$(34)
56030 dc%=cs%:gosub 56500
56040 print "{rvson}";chr$(34);ff$(cs%);chr$(34);"{rvsoff}"
56050 get a$:if a$="" then 56050
56060 a%=asc(a$)
56070 if a%=140 or a%=88 then run
56080 if a%=17 then if cs%+2<fc% then os%=cs%:cs%=cs%+2:goto 56000
56090 if a%=145 then if cs%-2>=0 then os%=cs%:cs%=cs%-2:goto 56000
56100 if a%=29 then if cs%+1><fc% then os%=cs%:cs%=cs%+1:goto 56000
56110 if a%=157 then if cs%-1>=0 then os%=cs%:cs%=cs%-1:goto 56000
56120 if a%=13 then 54000
56130 goto 56050

56500 rem calculate down and tab
56510 ta%=22*(dc% and 1)
56520 dc%=dc%/2+2
56530 dp$=left$("{25*down}", dc%)
56540 print chr$(19);tab(ta%);dp$;:return

57000 rem process file name
57005 if len(ff$)<2 then 57060
57010 for p=1 to len(ff$):if mid$(ff$,p,1)=chr$(34) then ff$=right$(ff$,len(ff$)-p):goto 57015
57012 next
57015 for p=1 to len(ff$)
57020 if mid$(ff$,p,1)=chr$(34) then ff$=left$(ff$,p-1):goto 57040
57030 next
57040 if ff$="localviewer" then 57060
57050 ff$(fc%)=ff$:if fc%<40 then fc%=fc%+1
57060 fl%=0:ff$="":return

58000 rem show matching files on disk
58005 print chr$(147);
58010 if fc%=0 then print "No koala painter images found!":gosub 11000:run
58015 poke 646,7:print "CRSR: select, RETURN: load, F8: back{down}":poke 646,1
58020 for i=0 to fc%-1:t%=22*(i and 1)
58030 print tab(t%);chr$(34);ff$(i);chr$(34);
58035 if t%>0 then print
58040 next:return

59000 rem print directory
59010 print chr$(147);"Reading file list...":fc%=0
59020 open 1,dn%,0,"$":poke 781,1:sys 65478:get a$,a$
59030 get a$,a$,h$,l$:if st then sys 65484:close 1:goto 59070
59040 bl%=asc(h$+ll$)+256*asc(l$+ll$):fl%=0:if bl%=40 then fl%=1:ff$=""
59050 get a$,b$:if b$ then ff$=ff$+a$+b$:if st=0 then if len(ff$)<100 then 59050
59060 ff$=ff$+a$:if fl%=1 then gosub 57000
59065 goto 59030
59070 gosub 58000
59090 gosub 55000
59100 return

60000 rem end program
60010 poke 45,0:poke 46,10:poke 47,0:poke 48,10:poke 49,0:poke 50,10
60020 poke 55,0:poke 56,160:poke 51,0:poke 52,160:print chr$(9);
60030 print:print "{down}Have a nice BASIC!":end