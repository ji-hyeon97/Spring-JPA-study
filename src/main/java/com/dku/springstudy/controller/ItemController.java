package com.dku.springstudy.controller;

import com.dku.springstudy.dto.*;
import com.dku.springstudy.model.Category;
import com.dku.springstudy.model.Images;
import com.dku.springstudy.model.Items;
import com.dku.springstudy.repository.ItemsRepository;
import com.dku.springstudy.service.ImageService;
import com.dku.springstudy.service.ItemsService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ItemController {
    private final ItemsService itemsService;
    private final ItemsRepository itemsRepository;
    private final ImageService imageService;

    @ApiOperation(value = "게시판 글쓰기", notes = "여러장의 이미지와 제목,가격,카테고리,게시글 내용 값을 받아서 게시판을 작성한뒤 게시판 id 반환")
    @PostMapping("/board")
    public ResponseDTO<?> uploadFile(@AuthenticationPrincipal String userId, ItemsDTO itemsDTO,
                                     @RequestPart("file") List<MultipartFile> file) {
        Long itemId = imageService.multipleUpload(file, userId, itemsDTO);
        return new ResponseDTO<>(HttpStatus.OK.value(), itemId);
    }

    /**
     * 화면에 맞는 dto, api스펙, 무한 스크롤 페이징 고민해야 한다
     * @return
     */
    @ApiOperation(value = "게시판 보기", notes = "사진,제목,가격,장소, 등을 게시판을 통해 보여준다")
    @GetMapping("/board")
    public ResponseDTO<?> index(){
        List<ItemsResponseDTO> result = itemsService.index();
        return new ResponseDTO<>(HttpStatus.OK.value(), result);
    }

    @ApiOperation(value = "상품 카데고리 보기", notes = "다양한 상품의 카테고리 정보를 제공한다")
    @GetMapping("/enum")
    public ResponseDTO<?> category(){
        return new ResponseDTO<>(HttpStatus.OK.value(), Category.values());
    }

    @ApiOperation(value = "상품 삭제하기", notes = "게시글에 올린 상품을 삭제할 수 있다")
    @DeleteMapping("/board/{itemId}")
    public ResponseDTO<?> delete(@AuthenticationPrincipal String userId, @PathVariable Long itemId){
        Items items = itemsRepository.findById(itemId).orElseThrow(()->new IllegalStateException("게시글에 올린 상품이 없음"));
        List<Images> images = items.getImages();
        imageService.deleteFile(images);
        itemsRepository.deleteById(itemId);
        return new ResponseDTO<>(HttpStatus.OK.value(), "삭제 완료");
    }

    @ApiOperation(value = "상품정보 수정하기", notes = "상품정보를 수정하며 이미지가 있는 경우 기존의 이미지 파일을 삭제합니다.")
    @PatchMapping("/board/{itemId}")
    public ResponseDTO<?> update(@AuthenticationPrincipal String userId, ItemsDTO itemsDTO,
                                 @RequestPart("file") List<MultipartFile> file, @PathVariable Long itemId) {

        imageService.multipleModify(file, userId, itemsDTO, itemId);
        return new ResponseDTO<>(HttpStatus.OK.value(), "수정완료");
    }

    @ApiOperation(value = "상품상태 변경하기", notes = "상품 상태를 판매중,판매완료,예약중 으로 설정할 수 있다")
    @PatchMapping("/board/{itemId}/status")
    public ResponseDTO<?> changeStatus(@PathVariable Long itemId, @RequestBody ItemsStatusDTO itemsStatusDTO){
        itemsService.changeItemStatus(itemId,itemsStatusDTO);
        return new ResponseDTO<>(HttpStatus.OK.value(), "상품상태 변경 완료");
    }

    @ApiOperation(value = "나의 판매상품 보기", notes = "내가 한 모든상품을 화면에 나타냅니다")
    @GetMapping("/board/myItem")
    public ResponseDTO<?> myItems(@AuthenticationPrincipal String userID){
        List<ItemsResponseDTO> myItems = itemsService.findMyItems(userID);
        return new ResponseDTO<>(HttpStatus.OK.value(), myItems);
    }

    @ApiOperation(value = "상품 상세보기", notes = "상품의 정보, 글쓴, 등 상품상세보기 화면 페이지를 구상합니다.")
    @GetMapping("/board/items/{itemId}")
    public ResponseDTO<?> productDetail(@PathVariable Long itemId){
        List<ItemDetailsResponseDTO> itemDetailsResponseDTOS = itemsService.productDetails(itemId);
        return new ResponseDTO<>(HttpStatus.OK.value(), itemDetailsResponseDTOS);
    }
}