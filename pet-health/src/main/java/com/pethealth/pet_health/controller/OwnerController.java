package com.pethealth.pet_health.controller;

import com.pethealth.pet_health.entity.Owner;
import com.pethealth.pet_health.service.OwnerService;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * OwnerController - 处理与主人相关的 REST API 请求
 */
@RestController
@RequestMapping("/owners")
public class OwnerController {

    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    /**
     * 获取所有主人
     */
    @GetMapping
    public List<Owner> getAllOwners() {
        return ownerService.getAllOwners();
    }

    /**
     * 添加新主人
     */
    @PostMapping
    public Owner addOwner(@RequestBody @NonNull Owner owner) {
        return ownerService.addOwner(owner);
    }

    /**
     * 根据 ID 获取主人
     */
    @GetMapping("/{id}")
    public Owner getOwner(@PathVariable @NonNull Long id) {
        return ownerService.getOwnerById(id);
    }

    /**
     * 更新主人信息
     */
    @PutMapping("/{id}")
    public Owner updateOwner(
            @PathVariable @NonNull Long id,
            @RequestBody @NonNull Owner updatedOwner
    ) {
        return ownerService.updateOwner(id, updatedOwner);
    }

    /**
     * 删除主人
     */
    @DeleteMapping("/{id}")
    public void deleteOwner(@PathVariable @NonNull Long id) {
        ownerService.deleteOwner(id);
    }

    /**
     * 根据姓名模糊搜索主人
     */
    @GetMapping("/search")
    public List<Owner> searchOwners(@RequestParam @NonNull String name) {
        return ownerService.searchOwnersByName(name);
    }
}
