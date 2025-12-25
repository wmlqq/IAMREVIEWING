package org.myself.iamreviewing.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.druid.sql.visitor.SQLEvalVisitorUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.myself.iamreviewing.domain.dto.PointDTO;
import org.myself.iamreviewing.domain.po.Attachment;
import org.myself.iamreviewing.domain.po.Point;
import org.myself.iamreviewing.domain.vo.AttachmentVO;
import org.myself.iamreviewing.domain.vo.PointVO;
import org.myself.iamreviewing.mapper.PointMapper;
import org.myself.iamreviewing.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.myself.iamreviewing.service.PointService;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PointServiceImpl extends ServiceImpl<PointMapper, Point> implements PointService {

    @Autowired
    private  AttachmentService attachmentService;

    //获取所有分类
    @Override
    public List<String> getAllCategories() {
        return listObjs(new LambdaQueryWrapper<Point>().select(Point::getCategory))
                .stream()
                .map(obj -> (String) obj)
                .distinct()
                .toList();
    }

    //通过关键字模糊查询
    @Override
    public List<PointVO> searchByKeyword(String keyword) {
        LambdaQueryWrapper<Point> queryWrapper=new LambdaQueryWrapper<Point>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String fuzzyKeyword = "%" + keyword.trim() + "%"; // 前后加%，全模糊匹配

            queryWrapper.like(Point::getName, fuzzyKeyword) // 匹配name字段
                    .or() // 条件连接：OR（满足任意一个即可）
                    .like(Point::getDescription, fuzzyKeyword) // 匹配description字段
                    .or()
                    .like(Point::getCategory, fuzzyKeyword); // 匹配category字段
        }
        return convertToVO(list(queryWrapper));
    }

    //通过分类查询
    @Override
    public List<PointVO> getByCategory(String category) {
        LambdaQueryWrapper<Point> queryWrapper=new LambdaQueryWrapper<Point>().eq(Point::getCategory,category);

        return convertToVO(list(queryWrapper));
    }

    //将前端dto保存为po
    @Override
    public boolean saveDTO(PointDTO pointDTO) {
        Point point = BeanUtil.copyProperties(pointDTO, Point.class);
        return save(point);
    }

    @Override
    public List<PointVO> getAllPoints() {
        return convertToVO(list());
    }

    @Override
    public PointVO updatePoint(Long id, PointDTO pointDTO) {
        Point point = BeanUtil.copyProperties(pointDTO, Point.class);
        point.setId(id);
        updateById(point);
        return convertToVO(Optional.ofNullable(getById(id)).stream().collect(Collectors.toList())).get(0);
    }

    @Override
    public PointVO createPoint(PointDTO pointDTO) {
        Point point = BeanUtil.copyProperties(pointDTO, Point.class);
        save(point);
        return convertToVO(Optional.ofNullable(getById(point.getId())).stream().collect(Collectors.toList())).get(0);
    }


    public  List<PointVO> convertToVO(List<Point> points) {
        if(points==null||points.isEmpty()){return List.of();}

        List<PointVO> pointVOS = BeanUtil.copyToList(points, PointVO.class);

        for(PointVO pointVO:pointVOS){
            pointVO.setAttachmentVOS(attachmentService.getByPointId(pointVO.getId()));
        }
        return pointVOS;
    }
}
