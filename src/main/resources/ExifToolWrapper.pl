#!/usr/bin/perl

#-------------------------------------------------------------------------------
# Imports

# Safe mode
use strict;
use warnings;

# Add current directory to library path
use FindBin;
use lib "$FindBin::Bin";

# Import encode/decode functions
use Codec;

# Import ExifTool library
use lib 'C:\Apps\Image-ExifTool-10.23\lib'; # TODO: Remove this when library is being unpacked in same location
use Image::ExifTool qw(:Public);

#-------------------------------------------------------------------------------

# Create the ExifTool instance
my $tool = new Image::ExifTool;


# Read requests from STDIN until the stream is closed...
while(readBegin())
{
    # Read the first byte, which is the request type
    my $type = readByte();

    # Handle the request
    if ($type == REQUEST_SETOPTION)
    {
        setOption();
    }
    elsif ($type == REQUEST_CLEAROPTIONS)
    {
        clearOptions();
    }
    elsif ($type == REQUEST_EXTRACTINFO)
    {
        extractInfo();
    }
    else
    {
        die("Unknown request type: $type" );
    }
}

exit 0;

#-------------------------------------------------------------------------------
# Helper functions:

sub clearOptions
{
    # Read the rest of the request
    readEnd();

    # Perform action
    $tool->ClearOptions();

    # Send response
    sendOK();
}

sub setOption
{
    # Read the rest of the request
    my $name = readString();
    my $value = readString();
    readEnd();

    # Perform action
    $tool->Options( $name => $value );

    # Send response
    sendOK();
}

sub extractInfo
{
    # Read the rest of the request
    my $filename = readString();
    readEnd();

    # Perform action
    my $result = $tool->ExtractInfo( $filename );

    # Check result, then send response
    if ($result == 1)
    {
        # Get the desired tag values into a hash
        my $info = $tool->GetInfo(  ); # TODO: pass in desired tags
        sendTagInfo($info);
    }
    else
    {
        sendError( "Not a recognized file format" );
    }
}




