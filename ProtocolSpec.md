This is the ABNF definition of the request/response messages to be 
transmitted between the Java process and the Perl process.
---
Basic Rules:
```
NULL   = %x00      
BYTE   = %x00-FF    ; 8 bits of data
INT    = 4BYTE      ; 32-bit signed integer
CHAR   = %x01-FF    ; any Extended ASCII character, excluding NULL
STRING = *CHAR NULL ; a NULL-terminated character string  
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
               ExtractInfo
          
SetOption    = %x01   ; type = 1
               STRING ; option name
               STRING ; option value
                      ; ... Response = OK

ClearOptions = %x02 ; type = 2
                    ; ... Response = OK

SetTags      = %x03    ; type = 3
               INT     ; number of tag names
               *STRING ; tag names
                       ; ... Response = OK
               
ExtractInfo  = %x04   ; type = 4
               STRING ; filename
                      ; ... Response = TagInfo / Error
```

Responses:
```
Response     = BEGIN ResponseBody END
 
ResponseBody = OK /
               Error /
               TagInfo
               
OK           = %x01   ; type = 1
               
Error        = %x02   ; type = 2
               STRING ; message
               
TagInfo      = %x03   ; type = 3
               INT    ; number of tags
               *Tag   ; tags (name/value pairs)
               
Tag          = STRING                      ; name
               (StringValue / BinaryValue) ; value

StringValue  = %x01   ; type = 1
               STRING ; value

BinaryValue  = %x02   ; type = 2
               INT    ; data length (# of octets)
               *BYTE  ; data
```