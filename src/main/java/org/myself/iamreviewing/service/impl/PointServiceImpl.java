package org.myself.iamreviewing.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.myself.iamreviewing.domain.po.Point;
import org.myself.iamreviewing.mapper.PointMapper;
import org.springframework.stereotype.Service;
import org.myself.iamreviewing.service.PointService;

@Service
public class PointServiceImpl extends ServiceImpl<PointMapper, Point> implements PointService {
}
