#!/usr/bin/perl

#-------------------------------------------------------------------------------
# Imports

# Safe mode
use strict;
use warnings;

# Add current directory to library path
use lib ".";

# Import encode/decode functions
use Codec;

# Import ExifTool library
use Image::ExifTool qw(:Public);

#-------------------------------------------------------------------------------

# Create the ExifTool instance
my $tool = new Image::ExifTool;

# Repeatedly read requests from STDIN and send responses to STDOUT...
while(readBegin())
{
    # Read the first byte, which is the request type
    my $type = readByte();

    # Read/handle the rest of the request, based on type
    if ($type == REQUEST_SETOPTION)
    {
        setOption();
    }
    elsif ($type == REQUEST_CLEAROPTIONS)
    {
        clearOptions();
    }
    elsif ($type == REQUEST_SETTAGS)
    {
        setTags();
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

sub clearOptions
{
    # Read the rest of the request
    readEnd();

    # Perform action
    $tool->ClearOptions();

    # Send response
    sendOK();
}

sub setTags
{
    # Read the rest of the request
    my $count = readInt();
    my @tags = ();
    for (my $i = 0; $i < $count; $i++)
    {
        push( @tags, readString() );
    }
    readEnd();

    # Perform action
    $tool->Options( RequestAll => 0 );
    $tool->Options( RequestTags => \@tags );

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
        my $info = $tool->GetInfo();
        sendTagInfo($info);
    }
    else
    {
        sendError( "Not a recognized file format" );
    }
}




