
# ./make_icns.sh input_1024.png output.icns
# creates an .icns icon-set from a 1024x1024 pixel .png file. 
mkdir MyIcon.iconset
sips -z 16 16     $1 --out MyIcon.iconset/icon_16x16.png
sips -z 32 32     $1 --out MyIcon.iconset/icon_16x16@2x.png
sips -z 32 32     $1 --out MyIcon.iconset/icon_32x32.png
sips -z 64 64     $1 --out MyIcon.iconset/icon_32x32@2x.png
sips -z 128 128   $1 --out MyIcon.iconset/icon_128x128.png
sips -z 256 256   $1 --out MyIcon.iconset/icon_128x128@2x.png
sips -z 256 256   $1 --out MyIcon.iconset/icon_256x256.png
sips -z 512 512   $1 --out MyIcon.iconset/icon_256x256@2x.png
sips -z 512 512   $1 --out MyIcon.iconset/icon_512x512.png
cp $1 MyIcon.iconset/icon_512x512@2x.png
iconutil -c icns MyIcon.iconset
rm -R MyIcon.iconset
mv MyIcon.icns $2
