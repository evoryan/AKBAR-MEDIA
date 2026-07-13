import re

with open('app/src/main/java/com/example/ui/screens/CustomersScreen.kt', 'r') as f:
    content = f.read()

# Just verifying we don't have dangling Spacer
content = content.replace(
    'Spacer(modifier = Modifier.height(16.dp))\n                Text(text = customer.price, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textMain)\n                Text(text = customer.discount, fontSize = 12.sp, color = greenText)\n            }\n        }\n    }\n}',
    'Spacer(modifier = Modifier.height(16.dp))\n                Text(text = customer.price, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textMain)\n                Text(text = customer.discount, fontSize = 12.sp, color = greenText)\n            }\n        }\n    }\n}'
)
