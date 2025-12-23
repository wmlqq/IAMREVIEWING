package org.myself.iamreviewing.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.myself.iamreviewing.domain.po.Attachment;
import org.myself.iamreviewing.mapper.AttachmentMapper;
import org.springframework.stereotype.Service;
import org.myself.iamreviewing.service.AttachmentService;

@Service
public class AttachmentServiceImpl extends ServiceImpl<AttachmentMapper, Attachment> implements AttachmentService {
}
