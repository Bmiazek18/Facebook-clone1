package com.facebook.UserService;

import com.facebook.UserService.client.SearchServiceClient;
import com.facebook.UserService.client.SocialGraphClient;
import com.facebook.UserService.dto.RegisterRequest;
import com.facebook.UserService.model.SearchUser;
import com.facebook.UserService.model.User;
import com.facebook.UserService.repository.SearchUserRepository;
import com.facebook.UserService.repository.UserRepository;
import com.facebook.UserService.service.UserService;
import com.facebook.search.grpc.SearchUserHit;
import com.facebook.socialgraph.grpc.UserRelation;
import com.facebook.user.grpc.UserActiveStatusMessage;
import com.facebook.user.grpc.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
		"grpc.server.port=-1",
		"spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class UserServiceApplicationTests {

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private SearchUserRepository searchUserRepository;

	@Autowired
	private com.facebook.UserService.service.UserActiveService userActiveService;

	@org.springframework.boot.test.mock.mockito.MockBean
	private SocialGraphClient socialGraphClient;

	@org.springframework.boot.test.mock.mockito.MockBean
	private SearchServiceClient searchServiceClient;

	@org.springframework.boot.test.mock.mockito.MockBean
	private com.facebook.UserService.service.MinioService minioService;

	@org.springframework.boot.test.mock.mockito.MockBean
	private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

	private final java.util.Map<String, String> fakeRedis = new java.util.concurrent.ConcurrentHashMap<>();

	@org.junit.jupiter.api.BeforeEach
	void setUpRedisMock() {
		org.springframework.data.redis.core.ValueOperations<String, String> valOps = org.mockito.Mockito.mock(org.springframework.data.redis.core.ValueOperations.class);
		org.mockito.Mockito.when(redisTemplate.opsForValue()).thenReturn(valOps);

		// Mock set
		org.mockito.Mockito.doAnswer(invocation -> {
			String key = invocation.getArgument(0);
			String val = invocation.getArgument(1);
			fakeRedis.put(key, val);
			return null;
		}).when(valOps).set(org.mockito.Mockito.anyString(), org.mockito.Mockito.anyString(), org.mockito.Mockito.anyLong(), org.mockito.Mockito.any(java.util.concurrent.TimeUnit.class));

		// Mock get
		org.mockito.Mockito.when(valOps.get(org.mockito.Mockito.anyString())).thenAnswer(invocation -> {
			String key = invocation.getArgument(0);
			return fakeRedis.get(key);
		});

		// Mock multiGet
		org.mockito.Mockito.when(valOps.multiGet(org.mockito.Mockito.anyCollection())).thenAnswer(invocation -> {
			java.util.Collection<String> keys = invocation.getArgument(0);
			return keys.stream().map(fakeRedis::get).collect(java.util.stream.Collectors.toList());
		});
	}

	@Test
	void contextLoads() {
	}

	@Test
	void testSearchUserRecordsHistory() throws InterruptedException {
		RegisterRequest registerRequest = RegisterRequest.builder()
				.username("testsearcher")
				.email("testsearcher@example.com")
				.password("securepassword")
				.firstName("John")
				.lastName("Doe")
				.build();
		org.mockito.Mockito.doNothing().when(searchServiceClient).indexUser(
				org.mockito.Mockito.anyString(),
				org.mockito.Mockito.anyString(),
				org.mockito.Mockito.anyString(),
				org.mockito.Mockito.anyString(),
				org.mockito.Mockito.anyString()
		);

		userService.registerUser(registerRequest);

		Optional<User> userOpt = userRepository.findByUsername("testsearcher");
		assertTrue(userOpt.isPresent());
		User user = userOpt.get();
		assertEquals("John", user.getFirstName());
		assertEquals("Doe", user.getLastName());

		Optional<SearchUser> initialSearchOpt = searchUserRepository.findBySearchedUser(user);
		assertTrue(initialSearchOpt.isEmpty());

		UserDto searchResponse1 = userService.searchUserById(user.getId(), user.getId());
		assertNotNull(searchResponse1);
		assertEquals(user.getId().toString(), searchResponse1.getId());
		assertEquals("John", searchResponse1.getFirstName());
		assertEquals("Doe", searchResponse1.getLastName());

		Optional<SearchUser> searchOptAfter = searchUserRepository.findBySearchedUser(user);
		assertTrue(searchOptAfter.isPresent());
		LocalDateTime searchTime1 = searchOptAfter.get().getLastSearchedAt();
		assertNotNull(searchTime1);

		Thread.sleep(10);

		UserDto searchResponse2 = userService.searchUserById(user.getId(), user.getId());
		assertNotNull(searchResponse2);

		Optional<SearchUser> searchOptAfter2 = searchUserRepository.findBySearchedUser(user);
		assertTrue(searchOptAfter2.isPresent());
		LocalDateTime searchTime2 = searchOptAfter2.get().getLastSearchedAt();
		assertTrue(searchTime2.isAfter(searchTime1));

		org.mockito.Mockito.when(searchServiceClient.searchUsers("Joh")).thenReturn(java.util.List.of(
				SearchUserHit.newBuilder()
						.setId(user.getId().toString())
						.setUsername("testsearcher")
						.setFirstName("John")
						.setLastName("Doe")
						.build()
		));

		java.util.List<UserDto> searchResults = userService.searchUsers("Joh", null);
		assertFalse(searchResults.isEmpty());
		assertEquals("John", searchResults.get(0).getFirstName());
		assertEquals("Doe", searchResults.get(0).getLastName());

		java.util.List<UserDto> userHistory = userService.getSearchHistory(user.getId());
		assertEquals(1, userHistory.size());
		assertEquals(user.getId().toString(), userHistory.get(0).getId());

		java.util.List<UserDto> globalHistory = userService.getSearchHistory(null);
		assertFalse(globalHistory.isEmpty());
		assertEquals(user.getId().toString(), globalHistory.get(0).getId());
	}

	@Test
	void testSearchUsersWithSocialGraph() {
		User viewer = userRepository.save(User.builder()
				.username("viewer")
				.email("viewer@example.com")
				.password("pass")
				.firstName("Viewer")
				.lastName("User")
				.build());

		User friendUser = userRepository.save(User.builder()
				.username("j_friend")
				.email("friend@example.com")
				.password("pass")
				.firstName("John")
				.lastName("Friend")
				.build());

		User connectionUser = userRepository.save(User.builder()
				.username("connection")
				.email("connection@example.com")
				.password("pass")
				.firstName("Connection")
				.lastName("User")
				.build());

		User mutualFriendUser = userRepository.save(User.builder()
				.username("j_mutual")
				.email("mutual@example.com")
				.password("pass")
				.firstName("John")
				.lastName("Mutual")
				.build());

		User strangerUser = userRepository.save(User.builder()
				.username("j_stranger")
				.email("stranger@example.com")
				.password("pass")
				.firstName("John")
				.lastName("Stranger")
				.build());

		java.util.List<UserRelation> mockRelations = java.util.List.of(
				UserRelation.newBuilder()
						.setTargetUserId(friendUser.getId().toString())
						.setFriend(true)
						.setMutualFriendsCount(0)
						.build(),
				UserRelation.newBuilder()
						.setTargetUserId(connectionUser.getId().toString())
						.setFriend(false)
						.setMutualFriendsCount(0)
						.build(),
				UserRelation.newBuilder()
						.setTargetUserId(mutualFriendUser.getId().toString())
						.setFriend(false)
						.setMutualFriendsCount(1)
						.build(),
				UserRelation.newBuilder()
						.setTargetUserId(strangerUser.getId().toString())
						.setFriend(false)
						.setMutualFriendsCount(0)
						.build()
		);

		org.mockito.Mockito.when(searchServiceClient.searchUsers("John")).thenReturn(java.util.List.of(
				SearchUserHit.newBuilder()
						.setId(strangerUser.getId().toString())
						.setUsername("j_stranger")
						.setFirstName("John")
						.setLastName("Stranger")
						.build(),
				SearchUserHit.newBuilder()
						.setId(mutualFriendUser.getId().toString())
						.setUsername("j_mutual")
						.setFirstName("John")
						.setLastName("Mutual")
						.build(),
				SearchUserHit.newBuilder()
						.setId(connectionUser.getId().toString())
						.setUsername("connection")
						.setFirstName("Connection")
						.setLastName("User")
						.build(),
				SearchUserHit.newBuilder()
						.setId(friendUser.getId().toString())
						.setUsername("j_friend")
						.setFirstName("John")
						.setLastName("Friend")
						.build()
		));

		org.mockito.Mockito.when(socialGraphClient.getRelations(
				org.mockito.Mockito.eq(viewer.getId()),
				org.mockito.Mockito.anyList()
		)).thenReturn(mockRelations);

		java.util.List<UserDto> results = userService.searchUsers("John", viewer.getId());

		assertTrue(results.size() >= 3);
		assertEquals(friendUser.getId().toString(), results.get(0).getId());
		assertEquals(mutualFriendUser.getId().toString(), results.get(1).getId());
		assertEquals(strangerUser.getId().toString(), results.get(2).getId());
	}

	@Test
	void testUserActiveStatusRedis() {
		java.util.UUID testUser1 = java.util.UUID.randomUUID();
		java.util.UUID testUser2 = java.util.UUID.randomUUID();

		userActiveService.setUserActive(testUser1);

		UserActiveStatusMessage status = userActiveService.getActiveStatus(testUser1);
		assertNotNull(status);
		assertEquals(testUser1.toString(), status.getUserId());
		assertTrue(status.getActive());
		assertEquals("aktywny", status.getLastActiveText());
		assertTrue(status.getLastActiveTimestamp() > 0);

		java.util.List<UserActiveStatusMessage> statuses =
				userActiveService.getActiveStatuses(java.util.List.of(testUser1, testUser2));

		assertEquals(1, statuses.size());

		UserActiveStatusMessage status1 = statuses.get(0);
		assertEquals(testUser1.toString(), status1.getUserId());
		assertTrue(status1.getActive());
		assertEquals("aktywny", status1.getLastActiveText());
	}
}
