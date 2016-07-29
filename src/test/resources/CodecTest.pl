use strict;
use warnings;

use lib "C:/Projects/agaluzzi/kotlin-exiftool/src/main/resources";
use Codec;

readBegin() || die("Failed to read BEGIN");
my $intVal = readInt();
my $byteVal = readByte();
my $strVal = readString();
my $binVal = readBinary();
readEnd();

writeBegin();
writeInt( $intVal );
writeByte( $byteVal );
writeString( $strVal );
writeBinary( $binVal );
writeEnd();
flushOutput();
