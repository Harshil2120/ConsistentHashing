package com.cloudproject.ch;

import com.amazonaws.Response;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.elasticache.AmazonElastiCache;
import com.amazonaws.services.elasticache.AmazonElastiCacheClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import net.spy.memcached.MemcachedClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication(exclude = RedisAutoConfiguration.class)
@RestController
@RequestMapping("/api/v1")
public class KeyValueController {

	@Value("${dynamodb.endpoint}")
	private String dynamoDBEndpoint;

	@Value("${elasticache.node1.ip}")
	private String elasticacheNode1IP;

	@Value("${elasticache.node2.ip}")
	private String elasticacheNode2IP;

	@Value("${elasticache.node3.ip}")
	private String elasticacheNode3IP;



	List<String> elasticacheServerList;
	int SERVER_POINTER = 2;
	private final int NUMBER_OF_SERVERS = 5;

	private final AmazonElastiCache elastiCacheClient;
	private final DynamoDB dynamoDB;
	private final ConsistentHashManager consistentHashing;
	private MemcachedClient memcachedClient = null;

	@Autowired
	public KeyValueController(@Value("${dynamodb.endpoint}") String dynamoDBEndpoint,
							  @Value("${elasticache.node1.ip}") String elasticacheNode1IP,
							  @Value("${elasticache.node2.ip}") String elasticacheNode2IP,
							  @Value("${elasticache.node3.ip}") String elasticacheNode3IP) {
		AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(dynamoDBEndpoint, "us-east-2")) // Update region if necessary
				.build();

		this.elastiCacheClient = AmazonElastiCacheClientBuilder.defaultClient();
		this.dynamoDB = new DynamoDB(amazonDynamoDB);
		this.consistentHashing = new ConsistentHashManager();
		this.consistentHashing.addNode(elasticacheNode1IP);
		this.consistentHashing.addNode(elasticacheNode2IP);
		this.consistentHashing.addNode(elasticacheNode3IP);
		this.elasticacheServerList = new ArrayList<>();
		this.elasticacheServerList.add(elasticacheNode1IP);
		this.elasticacheServerList.add(elasticacheNode2IP);
		this.elasticacheServerList.add(elasticacheNode3IP);

	}

	@PostMapping("/add")
	public ResponseEntity<?> putKeyValue(@RequestBody Map<String, String> keyValue) {
		System.out.println("Received request body: " + keyValue);
		String key = keyValue.get("username");
		String value = keyValue.get("email");

		Table table = dynamoDB.getTable("emailTable");
		PutItemSpec putItemSpec = new PutItemSpec().withItem(new Item().withPrimaryKey("username", key).withString("email", value));
		table.putItem(putItemSpec);
		Map<String, String> responseData = new HashMap<>();
		responseData.put("Message", "Username and email stored successfully in DynamoDB!!");

		return ResponseEntity.status(HttpStatus.OK).body(responseData);
	}

	@PostMapping("/addserver")
	public String addNewCacheServer()
	{
		if (SERVER_POINTER == NUMBER_OF_SERVERS)
			return "You have run out of number of configured Elasticache servers!!!. Contact admin.";
		String newElasticacheServer = this.elasticacheServerList.get(SERVER_POINTER);
		this.consistentHashing.addNode(newElasticacheServer);
		SERVER_POINTER++;
		return "New Elasticache server added!!!";
	}

	@GetMapping("/{key}")
	public ResponseEntity<?> getKeyValue(@PathVariable String key)
			throws InterruptedException, IOException
	{
		String node = consistentHashing.getNode(key);
		memcachedClient = new MemcachedClient(new InetSocketAddress(node, 11211));
		String cachedValue;
		Map<String, String> responseData = new HashMap<>();
		try {
			cachedValue = (String) memcachedClient.get(key);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseData);
		}
		if (cachedValue != null)
		{
			responseData.put("ServerId", node);
			responseData.put("Email", cachedValue);
			return ResponseEntity.status(HttpStatus.OK).body(responseData);
		}

		Thread.sleep(10000);
		Table table = dynamoDB.getTable("emailTable");
		GetItemSpec getItemSpec = new GetItemSpec().withPrimaryKey("username", key);
		Item item = table.getItem(getItemSpec);
		responseData.put("ServerId", "DynamoDB");
		responseData.put("Message", "Database bottleneck introduced. Slept for 10s to imitate network latency!!!");
		if (item != null) {
			memcachedClient.set(key, 3600, item.getString("email"));
			responseData.put("Email", item.getString("email"));
			return ResponseEntity.status(HttpStatus.OK).body(responseData);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseData);
		}
	}

	@PutMapping("/{key}")
	public ResponseEntity<?> updateKeyValue(@PathVariable String key, @RequestBody Map<String, String> email)
			throws IOException
	{
		String node = consistentHashing.getNode(key);
		memcachedClient = new MemcachedClient(new InetSocketAddress(node, 11211));
		String value = email.get("email");
		Table table = dynamoDB.getTable("emailTable");
		PutItemSpec putItemSpec = new PutItemSpec().withItem(new Item().withPrimaryKey("username", key).withString("email", value));
		table.putItem(putItemSpec);
		memcachedClient.set(key, 3600, value);
		Map<String, String> responseData = new HashMap<>();
		responseData.put("Message", "Username and email updated successfully in both DynamoDB and Elasticache!!!");
		return ResponseEntity.status(HttpStatus.OK).body(responseData);
	}

	@DeleteMapping("/{key}")
	public ResponseEntity<?> deleteKeyValue(@PathVariable String key) throws IOException
	{
		String node = consistentHashing.getNode(key);

		memcachedClient = new MemcachedClient(new InetSocketAddress(node, 11211));
		memcachedClient.delete(key);
		consistentHashing.removeKey(key);
		Table table = dynamoDB.getTable("emailTable");
		DeleteItemSpec deleteItemSpec = new DeleteItemSpec().withPrimaryKey("username", key);
		table.deleteItem(deleteItemSpec);
		Map<String, String> responseData = new HashMap<>();
		responseData.put("Message", "Username and Email deleted successfully from DynamoDB and Elasticache!!!");
		return ResponseEntity.status(HttpStatus.OK).body(responseData);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry
						.addMapping("/**")
						.allowedOrigins("*")
						.allowedMethods("*")
						.allowedHeaders("*")
				;
			}
		};
	}






	public static void main(String[] args) {
		SpringApplication.run(KeyValueController.class, args);
	}
}
