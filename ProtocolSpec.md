Basic Rules:
```
NULL   = %x00      
BYTE   = %x00-FF    ; 8 bits of data
INT    = 4BYTE      ; 32-bit signed integer
CHAR   = %x01-FF    ; any Extended ASCII character, excluding NUL
STRING = *CHAR NULL ; a null-terminated character string  
```

Magic Numbers:
```
BEGIN = %x0B1E55ED ; start of a message
END   = %x00FADE00 ; end of a message
```

Requests:
```
Request      = BEGIN RequestBody  END
          
RequestBody  = SetOption /
               ClearOptions /
               ExtractInfo
          
SetOption    = %x01   ; type = 1
               STRING ; option name
               STRING ; option value
               ; result = OK

ClearOptions = %x02   ; type = 2
               ; result = OK

SetTags      = %x03    ; type = 3
               INT     ; number of tag names
               *STRING ; tag names
               ; result = OK
               
ExtractInfo  = %x04   ; type = 4
               STRING ; filename
               ; result = TagInfo | Error
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
               *Tag   ; tag names/values
               
Tag          = STRING                      ; tag name
               (StringValue / BinaryValue) ; tag value

StringValue  = %x01   ; value type = 1
               STRING ; value

BinaryValue  = %x02   ; value type = 2
               INT    ; data length (# of octets)
               *BYTE  ; data
```