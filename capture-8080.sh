tshark -i lo -f 'tcp port 8080' -T fields -e _ws.col.Protocol -e _ws.col.Info
