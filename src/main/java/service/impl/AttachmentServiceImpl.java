package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import domain.po.Attachment;
import mapper.AttachmentMapper;
import org.springframework.stereotype.Service;
import service.AttachmentService;

@Service
public class AttachmentServiceImpl extends ServiceImpl<AttachmentMapper, Attachment> implements AttachmentService {
}
