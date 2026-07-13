import re

with open('VPS/init.sql', 'r') as f:
    content = f.read()

# Change customers odp_id to INT
content = content.replace("    odp_id VARCHAR(50) DEFAULT \"\",\n    odp_port VARCHAR(10) DEFAULT \"\"",
                          "    odp_id INT DEFAULT NULL,\n    odp_port VARCHAR(10) DEFAULT \"\",\n    FOREIGN KEY (odp_id) REFERENCES odp_list(id) ON DELETE SET NULL")

with open('VPS/init.sql', 'w') as f:
    f.write(content)
