package miniLu.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import miniLu.demo.Repository.LinkRepository;
import miniLu.demo.dto.LinkDTO;
import miniLu.demo.entity.Link;
import miniLu.demo.util.Mapper;

@Service
public class LinkService {

    private final LinkRepository linkRepository;
    private final Mapper mapper;

    LinkService(LinkRepository linkRepository, Mapper mapper) {
        this.linkRepository = linkRepository;
        this.mapper = mapper;
    }
    
    public List<LinkDTO> findAllUsersLinksByEmail(String email) {
        List<LinkDTO> links = linkRepository.findByUserEmail(email).stream().map(link -> 
            mapper.map(link, LinkDTO.class)).collect(Collectors.toList());
        return links;
    }

    public LinkDTO getLinkByID(String shortLink) {
        LinkDTO linkDTO = linkRepository.findByShortLink(shortLink)
                .map(link -> mapper.map(link, LinkDTO.class))
                .orElse(null);
        return linkDTO;
    }

    public boolean isLinkOwnByUser(String email, String shortLink) {
        List<Link> links = linkRepository.findByUserEmail(email);
        for (Link link : links) if (link.getShortLink() == shortLink) return true;
        return false;
    }
}
