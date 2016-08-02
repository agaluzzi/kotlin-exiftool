Requests:
```
Request      = BEGIN RequestBody  END
          
RequestBody  = SetOption /
               ClearOptions /
               ExtractInfo
          
SetOption    = %x01    ; (1) type identifier
               STRING  ; option name
               STRING  ; option value

ClearOptions = %x02    ; (2) type identifier

SetTags      = %x03    ; (3) type identifier
               INT     ; number of tag names
               *STRING ; tag names

ExtractInfo  = %x04    ; (3) type identifier
               STRING  ; filename
```

Responses:
```
Response     = BEGIN ResponseBody END
 
ResponseBody = OK /
               Error /
               TagInfo
               
OK           = %x01   ; (1) type identifier
               
Error        = %x02   ; (1) type identifier
               STRING ; message
               
TagInfo      = %x03   ; (2) type identifier
               INT    ; number of tags
               *Tag   ; tag names/values
               
Tag          = STRING                      ; tag name
               (StringValue / BinaryValue) ; tag value

StringValue  = %x01   ; (1) type identifier
               STRING ; value

BinaryValue  = %x02   ; (2) type identifier
               INT    ; data length (# of octets)
               *BYTE  ; data
```

Magic Numbers:
```
BEGIN = %x0B1E55ED ; start of a message
END   = %x00FADE00 ; end of a message
```

Basic Rules:
```
NULL   = %x00      
BYTE   = %x00-FF    ; 8 bits of data
INT    = 4BYTE      ; 32-bit signed integer
CHAR   = %x01-FF    ; any Extended ASCII character, excluding NUL
STRING = *CHAR NULL ; a null-terminated character string  
```
