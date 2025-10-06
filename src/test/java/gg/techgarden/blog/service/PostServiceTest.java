package gg.techgarden.blog.service;

import gg.techgarden.blog.cache.profile.Profile;
import gg.techgarden.blog.cache.profile.ProfileRepository;
import gg.techgarden.blog.model.TopPostTimeframe;
import gg.techgarden.blog.persistence.entity.Post;
import gg.techgarden.blog.persistence.entity.PostBody;
import gg.techgarden.blog.persistence.entity.PostMetadata;
import gg.techgarden.blog.persistence.repository.PostMetadataRepository;
import gg.techgarden.blog.persistence.repository.PostRepository;
import gg.techgarden.blog.util.SecurityUtil;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class PostServiceTest {
    @Mock
    private PostMetadataRepository postMetadataRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private ProfileRepository profileRepository;
    @InjectMocks
    private PostService postService;
    private AutoCloseable mocks;

    private Profile profile;
    private Post post;
    private PostMetadata metadata;
    private PostBody body;
    private Pageable pageable;
    private UUID userId;
    private List<Runnable> postTestVerifications;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        profile = new Profile();
        post = new Post();
        metadata = new PostMetadata();
        body = new PostBody();
        post.setMetadata(metadata);
        post.setBody(body);
        pageable = PageRequest.of(0, 10);
        userId = UUID.randomUUID();
        postTestVerifications = new ArrayList<>();
    }

    @AfterEach
    void tearDown() throws Exception {
        for (Runnable verification : postTestVerifications) {
            verification.run();
        }
        verifyNoMoreInteractions(postMetadataRepository, postRepository, profileRepository);
        mocks.close();
        clearInvocations(postMetadataRepository, postRepository, profileRepository);
    }

    @Test
    void getAllPostMetadata_ReturnsPublicPosts() {
        Page<PostMetadata> expected = new PageImpl<>(List.of(metadata));
        when(postMetadataRepository.findAllByPublicPostIsTrue(pageable)).thenReturn(expected);
        Page<PostMetadata> result = postService.getAllPostMetadata(pageable);
        assertEquals(expected, result);
        assertEquals(1, result.getContent().size());
        assertSame(metadata, result.getContent().get(0));
        postTestVerifications.add(() -> verify(postMetadataRepository).findAllByPublicPostIsTrue(pageable));
    }

    @Test
    void getAllPostMetadataForCurrentUser_ReturnsUserPosts() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserSub).thenReturn(Optional.of(userId));
            when(profileRepository.existsById(userId)).thenReturn(true);
            when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
            Page<PostMetadata> expected = new PageImpl<>(List.of(metadata));
            when(postMetadataRepository.findAllByAuthor(profile, pageable)).thenReturn(expected);
            Page<PostMetadata> result = postService.getAllPostMetadataForCurrentUser(pageable);
            assertEquals(expected, result);
            assertEquals(1, result.getContent().size());
            assertSame(metadata, result.getContent().get(0));
            postTestVerifications.add(() -> {
                verify(postMetadataRepository).findAllByAuthor(profile, pageable);
                verify(profileRepository).existsById(userId);
                verify(profileRepository).findById(userId);
            });
        }
    }

    @Test
    void getAllPostMetadataForCurrentUser_ReturnsEmptyPageIfProfileNotFound() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserSub).thenReturn(Optional.of(userId));
            when(profileRepository.existsById(userId)).thenReturn(false);
            Page<PostMetadata> result = postService.getAllPostMetadataForCurrentUser(pageable);
            assertTrue(result.isEmpty());
            postTestVerifications.add(() -> verify(profileRepository).existsById(userId));
        }
    }

    @Test
    void getAllPostMetadataForCurrentUser_ThrowsForbiddenIfUserNotAuthenticated() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserSub).thenReturn(Optional.empty());
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> postService.getAllPostMetadataForCurrentUser(pageable));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
            postTestVerifications.add(() -> verifyNoInteractions(postMetadataRepository, postRepository, profileRepository));
        }
    }
    @Test
    void getPosts_ReturnsAllPosts() {
        Page<Post> expected = new PageImpl<>(List.of(post));
        when(postRepository.findAll(pageable)).thenReturn(expected);
        Page<Post> result = postService.getPosts(pageable);
        assertEquals(expected, result);
        assertEquals(1, result.getContent().size());
        assertSame(post, result.getContent().get(0));
        postTestVerifications.add(() -> verify(postRepository).findAll(pageable));
    }

    @Test
    void createPost_SetsIdsToNullAndSavesPost() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            post.setId(UUID.randomUUID());
            metadata.setId(UUID.randomUUID());
            body.setId(UUID.randomUUID());
            securityUtil.when(SecurityUtil::getCurrentUserSub).thenReturn(Optional.of(userId));
            when(profileRepository.existsById(userId)).thenReturn(true);
            when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
            when(postRepository.save(post)).thenReturn(post);
            Post result = postService.createPost(post);
            assertNull(post.getId());
            assertNull(metadata.getId());
            assertNull(body.getId());
            assertEquals(post, result);
            postTestVerifications.add(() -> {
                verify(postRepository).save(post);
                verify(profileRepository).existsById(userId);
                verify(profileRepository).findById(userId);
            });
        }
    }

    @Test
    void updatePost_ThrowsNotFoundIfPostDoesNotExist() {
        UUID postId = UUID.randomUUID();
        post.setId(postId);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> postService.updatePost(post));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        postTestVerifications.add(() -> verify(postRepository).findById(postId));
    }

    @Test
    void updatePost_ThrowsNotFoundIfGetPostByIdReturnsNull() {
        UUID postId = UUID.randomUUID();
        post.setId(postId);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> postService.updatePost(post));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        postTestVerifications.add(() -> verify(postRepository).findById(postId));
    }

    @Test
    void updatePost_SavesPostIfExists() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            UUID postId = UUID.randomUUID();
            post.setId(postId);
            securityUtil.when(SecurityUtil::getCurrentUserSub).thenReturn(Optional.of(userId));
            when(postRepository.findById(postId)).thenReturn(Optional.of(post));
            when(profileRepository.existsById(userId)).thenReturn(true);
            when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
            when(postRepository.save(post)).thenReturn(post);
            Post result = postService.updatePost(post);
            assertEquals(post, result);
            postTestVerifications.add(() -> {
                verify(postRepository).findById(postId);
                verify(postRepository).save(post);
                verify(profileRepository).existsById(userId);
                verify(profileRepository).findById(userId);
            });
        }
    }

    @Test
    void savePost_SetsMetadataAndBodyReferencesAndSaves() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserSub).thenReturn(Optional.of(userId));
            when(profileRepository.existsById(userId)).thenReturn(true);
            when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
            when(postRepository.save(post)).thenReturn(post);
            Post result = postService.savePost(post);
            assertEquals(post, result);
            assertEquals(post, metadata.getPost());
            assertEquals(post, body.getPost());
            postTestVerifications.add(() -> {
                verify(postRepository).save(post);
                verify(profileRepository).existsById(userId);
                verify(profileRepository).findById(userId);
            });
        }
    }

    @Test
    void setOrUpdateProfile_ThrowsBadRequestIfMetadataIsNull() {
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> postService.setOrUpdateProfile(null));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        postTestVerifications.add(() -> verifyNoInteractions(postMetadataRepository, postRepository, profileRepository));
    }

    @Test
    void setOrUpdateProfile_ThrowsForbiddenIfUserNotAuthenticated() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserSub).thenReturn(Optional.empty());
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> postService.setOrUpdateProfile(metadata));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
            postTestVerifications.add(() -> verifyNoInteractions(postMetadataRepository, postRepository, profileRepository));
        }
    }

    @Test
    void setOrUpdateProfile_SavesProfileIfNotExistsAndSetsAuthor() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserSub).thenReturn(Optional.of(userId));
            securityUtil.when(SecurityUtil::getCurrentUserProfile).thenReturn(Optional.of(profile));
            when(profileRepository.existsById(userId)).thenReturn(false);
            when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
            when(profileRepository.save(profile)).thenReturn(profile);
            postService.setOrUpdateProfile(metadata);
            assertEquals(profile, metadata.getAuthor());
            postTestVerifications.add(() -> {
                verify(profileRepository).existsById(userId);
                verify(profileRepository).save(profile);
                verify(profileRepository).findById(userId);
            });
        }
    }

    @Test
    void setOrUpdateProfile_SetsAuthorIfProfileExists() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserSub).thenReturn(Optional.of(userId));
            when(profileRepository.existsById(userId)).thenReturn(true);
            when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
            postService.setOrUpdateProfile(metadata);
            assertEquals(profile, metadata.getAuthor());
            postTestVerifications.add(() -> {
                verify(profileRepository).existsById(userId);
                verify(profileRepository).findById(userId);
            });
        }
    }

    @Test
    void getPostById_ReturnsPostIfExists() {
        UUID postId = UUID.randomUUID();
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        Post result = postService.getPostById(postId);
        assertEquals(post, result);
        postTestVerifications.add(() -> verify(postRepository).findById(postId));
    }

    @Test
    void getPostById_ThrowsNotFoundIfPostDoesNotExist() {
        UUID postId = UUID.randomUUID();
        when(postRepository.findById(postId)).thenReturn(Optional.empty());
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> postService.getPostById(postId));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        postTestVerifications.add(() -> verify(postRepository).findById(postId));
    }
}