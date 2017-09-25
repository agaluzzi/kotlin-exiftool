This is the ABNF definition of the request/response messages to be transmitted 
between the Java process and the Perl process that leverages the exiftool library.
---
Basic Rules:
```
BYTE   = %x00-FF   ; 8 bits of data
INT    = 4BYTE     ; 32-bit signed integer
CHAR   = BYTE      ; any Extended ASCII character
STRING = INT *CHAR ; a length-prefixed character string  
BINARY = INT *BYTE ; a length-prefixed byte array
```

Magic Numbers:
```
BEGIN = %x0B1E55ED ; start of a message
END   = %x00FADE00 ; end of a message
```

Requests:
```
Request      = BEGIN RequestBody END
          
RequestBody  = SetOption /
               ClearOptions /
               SetTags /
               ExtractInfo /
               Test
          
SetOption    = %x01   ; type
               STRING ; option name
               STRING ; option value
                      ; ... Response = OK

ClearOptions = %x02 ; type
                    ; ... Response = OK

SetTags      = %x03    ; type
               INT     ; number of tag names
               *STRING ; tag names
                       ; ... Response = OK
               
ExtractInfo  = %x04   ; type
               STRING ; filename
                      ; ... Response = TagInfo
                      
Test         = %xAA   ; type
               BYTE   ; 8-bit integer 
               INT    ; 32-bit integer
               STRING ; character string
               BINARY ; binary data 
                      ; ... Response = Echo
```

Responses:
```
Response     = BEGIN ResponseBody END
 
ResponseBody = OK /
               TagInfo /
               Error
               
OK           = %x01   ; type
               
TagInfo      = %x02   ; type
               BYTE   ; result: 1 = success, 2 = unrecognized file format
               INT    ; number of tags
               *Tag   ; tags (name/value pairs)
               
Tag          = STRING                      ; name
               (StringValue / BinaryValue) ; value

StringValue  = %x01   ; type
               STRING ; value

BinaryValue  = %x02   ; type
               BINARY ; data
               
Error        = %xFF   ; type
               STRING ; message
               
Echo         = %xAA   ; type
               BYTE   ; 8-bit integer 
               INT    ; 32-bit integer
               STRING ; character string
               BINARY ; binary data 
```