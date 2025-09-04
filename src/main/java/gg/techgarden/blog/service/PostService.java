package gg.techgarden.blog.service;

import gg.techgarden.blog.model.PostBodyType;
import gg.techgarden.blog.persistence.entity.Post;
import gg.techgarden.blog.persistence.entity.PostBodyJson;
import gg.techgarden.blog.persistence.entity.PostMetadata;
import gg.techgarden.blog.persistence.repository.PostBodyJsonRepository;
import gg.techgarden.blog.persistence.repository.PostMetadataRepository;
import gg.techgarden.blog.persistence.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostMetadataRepository postMetadataRepository;
    private final PostRepository postRepository;
    private final PostBodyJsonRepository postBodyJsonRepository;

    public Page<PostMetadata> getAllPostMetadata(Pageable pageable) {
        return postMetadataRepository.findAll(pageable);
    }

    public Page<Post> getPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    @Transactional
    public Post createPost(Post post) {
        post.setId(null);
        post.getMetadata().setId(null);
        post.getBody().setId(null);
        return savePost(post);
    }

    @Transactional
    public Post updatePost(Post post) {
        if (getPostById(post.getId()) == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
        postBodyJsonRepository.deleteAllByPostId(post.getId());
        return savePost(post);
    }

    public Post savePost(Post post) {
        post.setPostBodyJson(getPostBodyFromContent(post.getBody().getContent()));
        post.getMetadata().setPost(post);
        post.getBody().setPost(post);
        post.getPostBodyJson().forEach(postBodyJson -> postBodyJson.setPost(post));
        return postRepository.save(post);
    }

    public Post getPostById(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));
    }

    private List<PostBodyJson> getPostBodyFromContent(String content) {
        List<PostBodyJson> posts = new ArrayList<>();
        if (StringUtils.hasText(content)) {
            List<String> paragraphs = List.of(content.split("\n"));
            int lineNumber = 1;
            for (int i = 0; i < paragraphs.size(); i++, lineNumber++) {
                PostBodyJson postBodyJson = new PostBodyJson();
                String paragraph = paragraphs.get(i);
                if (!StringUtils.hasText(paragraph)) {
                    continue;
                }
                paragraph = paragraph.trim();

                if (paragraph.startsWith("######")) {
                    postBodyJson.setType(PostBodyType.HEADING);
                    postBodyJson.setSubtype("h6");
                    paragraph = paragraph.substring("######".length()).trim();
                    postBodyJson.setText(paragraph);
                } else if (paragraph.startsWith("#####")) {
                    postBodyJson.setType(PostBodyType.HEADING);
                    postBodyJson.setSubtype("h5");
                    paragraph = paragraph.substring("#####".length()).trim();
                    postBodyJson.setText(paragraph);
                } else if (paragraph.startsWith("####")) {
                    postBodyJson.setType(PostBodyType.HEADING);
                    postBodyJson.setSubtype("h4");
                    paragraph = paragraph.substring("####".length()).trim();
                    postBodyJson.setText(paragraph);
                } else if (paragraph.startsWith("###")) {
                    postBodyJson.setType(PostBodyType.HEADING);
                    postBodyJson.setSubtype("h3");
                    paragraph = paragraph.substring("###".length()).trim();
                    postBodyJson.setText(paragraph);
                } else if (paragraph.startsWith("##")) {
                    postBodyJson.setType(PostBodyType.HEADING);
                    postBodyJson.setSubtype("h2");
                    paragraph = paragraph.substring("##".length()).trim();
                    postBodyJson.setText(paragraph);
                } else if (paragraph.startsWith("#")) {
                    postBodyJson.setType(PostBodyType.HEADING);
                    postBodyJson.setSubtype("h1");
                    paragraph = paragraph.substring("#".length()).trim();
                    postBodyJson.setText(paragraph);
                } else if (paragraph.startsWith("```")) {
                    int codeBlockEnd = -1;
                    for (int j = i + 1; j < paragraphs.size(); j++) {
                        String nextLine = paragraphs.get(j);
                        if (nextLine.trim().startsWith("```")) {
                            codeBlockEnd = j;
                            break;
                        }
                    }
                    if (codeBlockEnd == -1) {
                        postBodyJson.setType(PostBodyType.PARAGRAPH);
                        postBodyJson.setText(paragraph);
                    } else {
                        postBodyJson.setType(PostBodyType.CODE_BLOCK);
                        paragraph = paragraph.substring("```".length()).trim();
                        postBodyJson.setSubtype(paragraph);
                        StringBuilder codeContent = new StringBuilder();
                        for (int j = i + 1; j < codeBlockEnd; j++) {
                            codeContent.append(paragraphs.get(j)).append("\n");
                        }
                        postBodyJson.setText(codeContent.toString());
                        i = codeBlockEnd;
                    }
                } else if (paragraph.matches("^([>\\s]+)(\\s+.*)")) {
                    postBodyJson.setType(PostBodyType.QUOTE);
                    postBodyJson.setText(getPostBodyJsonText(paragraphs, "^([>\\s]+)(\\s+.*)", i, ">>", "<<", "", "", ">"));
                    while (paragraphs.get(i).trim().matches("^([>\\s]+)(\\s+.*)")) {
                        i++;
                        if (i >= paragraphs.size()) {
                            break;
                        }
                    }
                } else if (paragraph.matches("^([-+*])(\\s+.*)")) {
                    postBodyJson.setType(PostBodyType.UNORDERED_LIST);
                    postBodyJson.setText(getPostBodyJsonText(paragraphs, "^([-+*])(\\s+.*)", i, "+u", "-u", "+i", "-i", "  "));
                    while (paragraphs.get(i).trim().matches("^([-+*])(\\s+.*)")) {
                        i++;
                        if (i >= paragraphs.size()) {
                            break;
                        }
                    }
                } else if (paragraph.matches("^(\\d+\\.)(\\s+.*)")) {
                    postBodyJson.setType(PostBodyType.ORDERED_LIST);
                    postBodyJson.setText(getPostBodyJsonText(paragraphs, "^(\\d+\\.)(\\s+.*)", i, "+o", "-o", "+i", "-i", "  "));
                    postBodyJson.setSubtype(paragraph.replaceFirst("^(\\d+)\\.(\\s+.*)", "$1").trim());
                    while (paragraphs.get(i).trim().matches("^(\\d+\\.)(\\s+.*)")) {
                        i++;
                        if (i >= paragraphs.size()) {
                            break;
                        }
                    }
                } else if (paragraph.equals("---") || paragraph.equals("***") || paragraph.equals("___")) {
                    postBodyJson.setType(PostBodyType.DIVIDER);
                } else {
                    postBodyJson.setType(PostBodyType.PARAGRAPH);
                    postBodyJson.setText(paragraph);
                }

                postBodyJson.setLineNumber(lineNumber);
                posts.add(postBodyJson);
            }
        }
        return posts;
    }

    private String getPostBodyJsonText(List<String> paragraphs, String startsWith, int startIndex, String containerStart, String containerEnd, String inlineStart, String inlineEnd, String indentString) {
        String paragraph;
        int lastIndent = -1;
        int startsAdded = 0;
        StringBuilder quoteContent = new StringBuilder();
        for (int j = startIndex; j < paragraphs.size(); j++) {
            paragraph = StringUtils.trimTrailingCharacter(paragraphs.get(j),' ');
            if (!paragraph.trim().matches(startsWith)) {
                break;
            }
            int indentLevel = 0;
            while (paragraph.startsWith(indentString)) {
                indentLevel++;
                if (!indentString.isEmpty() && indentString.isBlank()) {
                    paragraph = StringUtils.trimTrailingCharacter(paragraph.substring(indentString.length()),' ');
                } else {
                    paragraph = paragraph.substring(indentString.length()).trim();
                }
            }
            if (paragraph.matches(startsWith)) {
                paragraph = paragraph.replaceFirst(startsWith, "$2").trim();
            }
            if (lastIndent < indentLevel) {
                quoteContent.append(containerStart).append("\n");
                startsAdded++;
            }
            if (lastIndent > indentLevel) {
                for (int k = indentLevel; k < (lastIndent); k++) {
                    quoteContent.append(containerEnd).append("\n");
                    startsAdded--;
                }
            }
            if (inlineStart != null && !inlineStart.isEmpty()) {
                quoteContent.append(inlineStart).append("\n");
            }
            quoteContent.append(paragraph).append("\n");
            if (inlineStart != null && !inlineStart.isEmpty()) {
                quoteContent.append(inlineEnd).append("\n");
            }
            lastIndent = indentLevel;
        }
        while (startsAdded > 0) {
            quoteContent.append(containerEnd).append("\n");
            startsAdded--;
        }
        return quoteContent.toString();
    }
}
