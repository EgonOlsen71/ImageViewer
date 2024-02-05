call mospeed ..\imageviewer.bas -inlineasm=true -generatesrc=true -varend=23540 -compactlevel=4 -assignmentopt=true -varopt=true -sysbuffer=53000
call mospeed ..\localviewer.bas -inlineasm=true -generatesrc=true -varend=23540 -compactlevel=4 -assignmentopt=true -compression=true
call moscrunch ++imageviewer.prg -addfiles=..\res\universal.prg
del imageviewer.d64
..\res\c1541 -format imageviewer,ml d64 imageviewer.d64
call ..\res\c1541 ..\build\imageviewer.d64 -write ++imageviewer-c.prg imageviewer,p
call ..\res\c1541 ..\build\imageviewer.d64 -write ++localviewer-c.prg localviewer,p
cd ..\build
