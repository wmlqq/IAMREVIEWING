package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import domain.po.Point;
import mapper.PointMapper;
import org.springframework.stereotype.Service;
import service.PointService;

@Service
public class PointServiceImpl extends ServiceImpl<PointMapper, Point> implements PointService {
}
