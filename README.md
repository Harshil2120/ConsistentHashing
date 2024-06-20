## `Java-Backend`

1. Set up a table on Amazon DynamoDB named "emailTable" (It is a serverless offering and so you should be directly able to create a table.)

2. Set up three Amazon Elasticache clusters (use "memcache" as the engine. (Don't use the serverless instance in this case due to connectivity issues. Choose "Design your own cluster". Add the configuration endpoint in application.properties.)

3. Set up an EC2 instance. Use "scp" to transfer your code into the instance.

4. Download Java 17 and Maven on the EC2 instance. Run "mvn package" inside the code directory.

5. Add your access_key and secret_key environment variables to EC2 (for connecting to the DynamoDB). Also attach an IAM role to this instance for full access to Elasticache.

6. Add an inbound rule to the security group of this EC2 in case you want to access the applicaton from outside the AWS cluster. It should be "Custom TCP rule" with port ranges "8080" and source as 0.0.0.0/0

7. "cd" into the target folder and run "java -jar <jar_file>".

## `NextJs-Frontend`

Setup:

```bash
# Install all the libraries
npm install

# Run the dev server
npm run dev
```

Make changes in `/.env.local`.
Add your ip address and port number

```bash

# NEXT_PUBLIC_API_URL= <Ip:address>:<port>
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the UI.
