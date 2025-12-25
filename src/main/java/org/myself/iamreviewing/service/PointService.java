package org.myself.iamreviewing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.myself.iamreviewing.domain.dto.PointDTO;
import org.myself.iamreviewing.domain.po.Point;
import org.myself.iamreviewing.domain.vo.PointVO;

import java.util.List;

public interface PointService extends IService<Point> {
    List<String> getAllCategories();

    List<PointVO> searchByKeyword(String keyword);

    List<PointVO> getByCategory(String category);

    boolean saveDTO(PointDTO pointDTO);

    List<PointVO> getAllPoints();

    PointVO updatePoint(Long id, PointDTO pointDTO);

    PointVO createPoint(PointDTO pointDTO);

    PointVO getPointById(Long id);

}
