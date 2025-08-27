JavaDoc comments must follow these quality standards:

- Be concise and substantive, capturing the essence without excessive detail
- Focus on what the method/class does, not how it does it
- Include essential information: purpose, parameters, return values, exceptions
- Avoid verbose descriptions and obvious statements
- Use clear, professional English language
- Follow standard JavaDoc format and tags
- Keep comments up-to-date with code changes
- Prefer meaningful descriptions to generic ones
- Try to avoid HTML tags in comments, but if description is large and necessary, 
- then it's acceptable
- All classes, methods, and fields must have JavaDoc comments (for check 
  style rule)

Required tags to use:
- @param - for parameter descriptions
- @return - for return value descriptions
- @throws - for exception descriptions

Examples of good JavaDoc:
- "Calculates the total price including tax for the given items"
- "Validates user input and returns sanitized data"
- "Retrieves user profile by email address"

Examples to avoid:
- "This method does something"
- "This is a getter method"
- "This method returns a value"

Principle: Use HTML tags only when absolutely necessary for better understanding 
of complex logic or large descriptions.