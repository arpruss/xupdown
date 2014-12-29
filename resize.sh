mkdir res/drawable-xxhdpi
mkdir res/drawable-xhdpi
mkdir res/drawable-hdpi
mkdir res/drawable-mdpi
mkdir res/drawable-ldpi

convert icon512.png -resize 144x144 res/drawable-xxhdpi/icon.png
convert icon512.png -resize 96x96 res/drawable-xhdpi/icon.png
convert icon512.png -resize 72x72 res/drawable-hdpi/icon.png
convert icon512.png -resize 48x48 res/drawable-mdpi/icon.png
convert icon512.png -resize 36x36 res/drawable-ldpi/icon.png