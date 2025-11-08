package com.ibrasoft.commandcentre.controller;

import com.ibrasoft.commandcentre.service.DiscordMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discord")
public class DiscordMappingController {

    @Autowired
    private DiscordMappingService discordService;

    // Get single nickname
    @GetMapping("/users/{userId}")
    public String getNickname(@PathVariable String userId) {
        return discordService.getNickname(userId);
    }

    // Get single role name
    @GetMapping("/roles/{roleId}")
    public String getRoleName(@PathVariable String roleId) {
        return discordService.getRoleName(roleId);
    }

    // Bulk lookup for nicknames
    @PostMapping("/users/bulk")
    public Map<String, String> bulkGetNicknames(@RequestBody List<String> userIds) {
        return discordService.getNicknames(userIds);
    }

    // Bulk lookup for role names
    @PostMapping("/roles/bulk")
    public Map<String, String> bulkGetRoleNames(@RequestBody List<String> roleIds) {
        return discordService.getRoleNames(roleIds);
    }
}