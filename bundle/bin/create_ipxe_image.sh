#!/bin/sh

## 28.11.2013 - Jens Röwekamp
## This script creates the ipxe image to boot from several plattforms.

if [ -z $1 ]
then
echo "Error: $0 URL platform\n"
echo "URL missing"
echo "e.G. $0 http://192.168.42.1:55555/boot.php bin/ipxe.kpxe"
exit 2
fi

if [ -z $2 ]
then
echo "Error: $0 URL platform\n"
echo "Plattform missing"
echo "e.G. $0 http://192.168.42.1:55555/boot.php bin/ipxe.kpxe for the tftp BIOS image."
echo "More information under http://ipxe.org/download"
exit 2
fi

## Check if ipxe exists
[ -d ../ipxe ]
if [ $? -gt 0 ]
then
	git clone git://git.ipxe.org/ipxe.git ../ipxe
	if [ $? -gt 0 ]
	then
		echo "Error: $0 URL platform\n"
		echo "../ipxe repository missing."
		echo "Please download from http://ipxe.org/download or install git"
		exit 2
	fi
fi

cat << EOF > ../ipxe/script.ipxe
#!ipxe
 
:retry_dhcp 
dhcp || goto retry_dhcp
chain $1?mac=\${net0/mac}
EOF

if [ $? -gt 0 ]
then
echo "Error: $? URL platform\n"
echo "File ../ipxe/script.ipxe not writeable"
exit 2
fi

## Activating more protocols/options for the iPXE image
cp ../ipxe/src/config/general.h ../ipxe/src/config/general.h.org

sed -i 's/#undef	NET_PROTO_IPV6/#define	NET_PROTO_IPV6/' ../ipxe/src/config/general.h
sed -i 's/#undef	DOWNLOAD_PROTO_HTTPS/#define	DOWNLOAD_PROTO_HTTPS/' ../ipxe/src/config/general.h
sed -i 's/#undef	DOWNLOAD_PROTO_NFS/#define	DOWNLOAD_PROTO_NFS/' ../ipxe/src/config/general.h
sed -i 's/\/\/#undef	SANBOOT_PROTO_ISCSI/#define	SANBOOT_PROTO_ISCSI/' ../ipxe/src/config/general.h
sed -i 's/\/\/#undef	SANBOOT_PROTO_AOE/#define	SANBOOT_PROTO_AOE/' ../ipxe/src/config/general.h
sed -i 's/#define	CRYPTO_80211_WEP/#undef	CRYPTO_80211_WEP/' ../ipxe/src/config/general.h
sed -i 's/\/\/#define VLAN_CMD/#define	VLAN_CMD/' ../ipxe/src/config/general.h
sed -i 's/\/\/#define REBOOT_CMD/#define	REBOOT_CMD/' ../ipxe/src/config/general.h
sed -i 's/\/\/#define POWEROFF_CMD/#define	POWEROFF_CMD/' ../ipxe/src/config/general.h
sed -i 's/\/\/#define IMAGE_TRUST_CMD/#define	IMAGE_TRUST_CMD/' ../ipxe/src/config/general.h
sed -i 's/\/\/#define PING_CMD/#define	PING_CMD/' ../ipxe/src/config/general.h

make -C ../ipxe/src $2 EMBED=../script.ipxe

if [ $? -gt 0 ]
then
mv ../ipxe/src/config/general.h.org ../ipxe/src/config/general.h
rm ../ipxe/script.ipxe
make -C ../ipxe/src clean
echo ""
echo "Error: $? URL plattform\n"
echo "Make process failed. Please check missing dependecies on"
echo "http://ipxe.org/download"
exit 2
fi

##Cleaning
mv ../ipxe/src/$2 ../storage/TFTP
mv ../ipxe/src/config/general.h.org ../ipxe/src/config/general.h
rm ../ipxe/script.ipxe
make -C ../ipxe/src clean

echo "iPXE boot file $2 successful created."
