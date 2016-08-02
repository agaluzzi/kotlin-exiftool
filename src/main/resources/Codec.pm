# Safe mode
use warnings;
use strict;

#-------------------------------------------------------------------------------

use constant {

    # Magic numbers
    BEGIN_TOKEN          => 0x0B1E55ED,
    END_TOKEN            => 0x00FADE00,

    # Request types
    REQUEST_SETOPTION    => 1,
    REQUEST_CLEAROPTIONS => 2,
    REQUEST_SETTAGS      => 3,
    REQUEST_EXTRACTINFO  => 4,

    # Response types
    RESPONSE_OK          => 1,
    RESPONSE_ERROR       => 2,
    RESPONSE_TAGINFO     => 3,

    # Value types
    VALUE_STRING         => 1,
    VALUE_BINARY         => 2,
};

# Set binary mode for standard I/O streams (avoids issues with newline substitutions)
binmode(STDIN) || die "Failed to set binary mode for STDIN";
binmode(STDOUT) || die "Failed to set binary mode for STDOUT";

#-------------------------------------------------------------------------------
# General I/O

sub readN
{
    my $n = shift;
    my $result = read( STDIN, my $buffer, $n);
    if ($result == $n)
    {
        return $buffer;
    }
    elsif ($result == 0)
    {
        # End-of-stream
        return undef;
    }
    else
    {
        die( "Error reading from STDIN: $!");
    }
}

sub flushOutput
{
    flush STDOUT;
}

#-------------------------------------------------------------------------------
# 32-bit integers

sub tryReadInt
{
    my $buffer = readN(4);
    return ( defined $buffer) ? unpack( "l", $buffer ) : undef;
}

sub readInt
{
    my $value = tryReadInt();
    if (defined $value)
    {
        return $value;
    }
    else
    {
        die("Failed to read 32-bit integer (end-of-stream)");
    }
}

sub writeInt
{
    my $value = shift;
    my $buffer = pack( "l", $value );
    print STDOUT $buffer;
}

#-------------------------------------------------------------------------------
# 8-bit integers

sub readByte
{
    my $buffer = readN(1);
    if (defined $buffer)
    {
        return  unpack( "C", $buffer )
    }
    else
    {
        die("Failed to read 8-bit integer (end-of-stream)");
    }
}

sub writeByte
{
    my $value = shift;
    my $buffer = pack( "C", $value );
    print STDOUT $buffer;
}

#-------------------------------------------------------------------------------
# BEGIN magic number

sub readBegin
{
    my $value = tryReadInt();
    if (defined $value)
    {
        if ($value == BEGIN_TOKEN)
        {
            return 1;
        }
        else
        {
            die( "Input stream corrupted -- Wrong BEGIN token" );
        }
    }
    else
    {
        # end of stream
        return 0;
    }
}

sub writeBegin
{
    writeInt( BEGIN_TOKEN );
}

#-------------------------------------------------------------------------------
# END magic number

sub readEnd
{
    if (readInt() != END_TOKEN)
    {
        die( "Input stream corrupted -- Wrong END token" );
    }
}

sub writeEnd
{
    writeInt( END_TOKEN );
}

#-------------------------------------------------------------------------------
# Strings (null-terminated)

sub readString
{
    my $str;
    {
        local $/ = "\0";
        $str = <STDIN>;
    }
    if (!defined($str) || $str !~ /\0\z/)
    {
        die("Failed to read null-terminated string (end-of-stream)");
    }
    chop($str); # remove nul
    return $str;
}

sub writeString
{
    my $str = shift;
    print STDOUT $str."\0";
}

#-------------------------------------------------------------------------------
# Binary

sub readBinary
{
    my $length = readInt();
    my $data = readN($length);
    if (!defined $data)
    {
        die("Failed to read $length bytes of binary data (end-of-stream)");
    }
    return $data;
}


sub writeBinary
{
    my $data = shift;
    writeInt( length($data) );
    print STDOUT $data;
}

#-------------------------------------------------------------------------------
# Typed values

sub writeValue
{
    my $value = shift;

    if (ref $value eq 'SCALAR')
    {
        writeByte(VALUE_BINARY);
        writeBinary($$value);
    }
    else
    {
        writeByte(VALUE_STRING);
        writeString($value);
    }
}

#-------------------------------------------------------------------------------
# Reponses

sub sendOK
{
    writeBegin();
    writeByte(RESPONSE_OK);
    writeEnd();
    flushOutput();
}

sub sendError
{
    my $msg = shift;

    writeBegin();
    writeByte(RESPONSE_ERROR);
    writeString($msg);
    writeEnd();
    flushOutput();
}

sub sendTagInfo
{
    my $info_ref = shift;
    my %info = %$info_ref;

    writeBegin();
    writeByte(RESPONSE_TAGINFO);

    # Write the number of tags
    my @tagNames = keys %info;
    writeInt( scalar @tagNames );

    # Write each tag...
    foreach my $tag (@tagNames)
    {
        writeString($tag);
        writeValue($info{$tag});
    }

    writeEnd();
    flushOutput();
}

#-------------------------------------------------------------------------------

return 1;
