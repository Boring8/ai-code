/**
 * 可视化编辑器工具类
 * 处理iframe通信和元素选择逻辑
 */

export interface SelectedElement {
  tagName: string
  id: string
  className: string
  textContent: string
  outerHTML: string
  selector: string
  boundingRect: {
    top: number
    left: number
    width: number
    height: number
  }
}

export class VisualEditor {
  private iframe: HTMLIFrameElement | null = null
  private isEditMode = false
  private selectedElement: SelectedElement | null = null
  private messageListener: ((event: MessageEvent) => void) | null = null
  private onElementSelect: ((element: SelectedElement | null) => void) | null = null
  private onModeChange: ((isEditMode: boolean) => void) | null = null

  constructor() {
    this.setupMessageListener()
  }

  /**
   * 设置iframe引用
   */
  setIframe(iframe: HTMLIFrameElement) {
    this.iframe = iframe
  }

  /**
   * 设置回调函数
   */
  setCallbacks(
    onElementSelect: (element: SelectedElement | null) => void,
    onModeChange: (isEditMode: boolean) => void
  ) {
    this.onElementSelect = onElementSelect
    this.onModeChange = onModeChange
  }

  /**
   * 切换编辑模式
   */
  toggleEditMode() {
    this.isEditMode = !this.isEditMode
    this.onModeChange?.(this.isEditMode)
    
    if (this.isEditMode) {
      this.enableEditMode()
    } else {
      this.disableEditMode()
    }
  }

  /**
   * 启用编辑模式
   */
  private enableEditMode() {
    if (!this.iframe?.contentWindow) return

    // 向iframe发送启用编辑模式的消息
    this.iframe.contentWindow.postMessage({
      type: 'ENABLE_EDIT_MODE'
    }, '*')
  }

  /**
   * 禁用编辑模式
   */
  private disableEditMode() {
    if (!this.iframe?.contentWindow) return

    // 向iframe发送禁用编辑模式的消息
    this.iframe.contentWindow.postMessage({
      type: 'DISABLE_EDIT_MODE'
    }, '*')

    // 清除选中的元素
    this.clearSelection()
  }

  /**
   * 清除选中的元素
   */
  clearSelection() {
    this.selectedElement = null
    this.onElementSelect?.(null)

    if (!this.iframe?.contentWindow) return

    // 向iframe发送清除选择的消息
    this.iframe.contentWindow.postMessage({
      type: 'CLEAR_SELECTION'
    }, '*')
  }

  /**
   * 获取选中的元素
   */
  getSelectedElement(): SelectedElement | null {
    return this.selectedElement
  }

  /**
   * 获取编辑模式状态
   */
  getEditMode(): boolean {
    return this.isEditMode
  }

  /**
   * 设置消息监听器
   */
  private setupMessageListener() {
    this.messageListener = (event: MessageEvent) => {
      // 确保消息来源是iframe
      if (!this.iframe || event.source !== this.iframe.contentWindow) {
        return
      }

      const { type, data } = event.data

      switch (type) {
        case 'ELEMENT_SELECTED':
          this.selectedElement = data
          this.onElementSelect?.(data)
          break
        
        case 'ELEMENT_CLEARED':
          this.selectedElement = null
          this.onElementSelect?.(null)
          break
      }
    }

    window.addEventListener('message', this.messageListener)
  }

  /**
   * 销毁编辑器
   */
  destroy() {
    if (this.messageListener) {
      window.removeEventListener('message', this.messageListener)
      this.messageListener = null
    }
    
    this.disableEditMode()
    this.iframe = null
    this.onElementSelect = null
    this.onModeChange = null
  }

  /**
   * 生成iframe注入脚本
   * 这个脚本会被注入到iframe中，用于处理元素选择
   */
  static getInjectionScript(): string {
    return `
      (function() {
        // 防重复注入：如果已初始化过，直接退出
        if (window.__AI_VISUAL_EDITOR__) {
          return;
        }
        window.__AI_VISUAL_EDITOR__ = true;

        let isEditMode = false;
        let selectedElement = null;
        let hoverElement = null;
        
        // 不再修改被选元素的 border（避免“红线残留”）
        // 采用覆盖层绘制高亮框
        const HOVER_BORDER = '2px solid #1890ff';
        const SELECTED_BORDER = '3px solid #ff4d4f';
        // 兼容旧实现（曾直接写入元素 style.border 的值）
        const LEGACY_HOVER_BORDER = '2px solid #1890ff';
        const LEGACY_SELECTED_BORDER = '3px solid #ff4d4f';
        const OVERLAY_Z_INDEX = 2147483647;

        function clearLegacyBorder(element) {
          if (!element || !element.style) return;
          const b = element.style.border;
          if (b === LEGACY_HOVER_BORDER || b === LEGACY_SELECTED_BORDER) {
            element.style.border = '';
          }
        }

        function createOverlay(id, border) {
          let el = document.getElementById(id);
          if (el) return el;
          el = document.createElement('div');
          el.id = id;
          el.style.position = 'fixed';
          el.style.top = '0';
          el.style.left = '0';
          el.style.width = '0';
          el.style.height = '0';
          el.style.border = border;
          el.style.boxSizing = 'border-box';
          el.style.borderRadius = '2px';
          el.style.pointerEvents = 'none';
          el.style.zIndex = String(OVERLAY_Z_INDEX);
          el.style.display = 'none';
          // 轻微高亮效果
          el.style.boxShadow = '0 0 0 2px rgba(255,255,255,0.6) inset';
          document.documentElement.appendChild(el);
          return el;
        }

        const hoverOverlay = createOverlay('__ai_visual_hover_overlay__', HOVER_BORDER);
        const selectedOverlay = createOverlay('__ai_visual_selected_overlay__', SELECTED_BORDER);

        function hideOverlay(overlay) {
          overlay.style.display = 'none';
          overlay.style.width = '0';
          overlay.style.height = '0';
        }

        function positionOverlayForElement(overlay, element) {
          if (!element || !element.getBoundingClientRect) {
            hideOverlay(overlay);
            return;
          }
          const rect = element.getBoundingClientRect();
          if (!rect || rect.width === 0 && rect.height === 0) {
            hideOverlay(overlay);
            return;
          }
          overlay.style.display = 'block';
          overlay.style.top = rect.top + 'px';
          overlay.style.left = rect.left + 'px';
          overlay.style.width = rect.width + 'px';
          overlay.style.height = rect.height + 'px';
        }

        function updateOverlays() {
          if (!isEditMode) {
            hideOverlay(hoverOverlay);
            hideOverlay(selectedOverlay);
            return;
          }
          // 选中框优先
          if (selectedElement) {
            positionOverlayForElement(selectedOverlay, selectedElement);
          } else {
            hideOverlay(selectedOverlay);
          }

          // 悬浮框不覆盖选中元素
          if (hoverElement && hoverElement !== selectedElement) {
            positionOverlayForElement(hoverOverlay, hoverElement);
          } else {
            hideOverlay(hoverOverlay);
          }
        }

        function clearAllHighlights() {
          // 如果页面里还残留旧实现写入的 border，尽量清掉（只清理命中的那两个元素）
          if (hoverElement) clearLegacyBorder(hoverElement);
          if (selectedElement) clearLegacyBorder(selectedElement);
          hoverElement = null;
          selectedElement = null;
          hideOverlay(hoverOverlay);
          hideOverlay(selectedOverlay);
        }
        
        // 获取元素选择器
        function getElementSelector(element) {
          if (element.id) {
            return '#' + element.id;
          }
          
          if (element.className) {
            const classes = element.className.split(' ').filter(c => c.trim());
            if (classes.length > 0) {
              return element.tagName.toLowerCase() + '.' + classes.join('.');
            }
          }
          
          // 回退到标签名
          let selector = element.tagName.toLowerCase();
          let parent = element.parentElement;
          
          if (parent) {
            const siblings = Array.from(parent.children).filter(
              child => child.tagName === element.tagName
            );
            
            if (siblings.length > 1) {
              const index = siblings.indexOf(element) + 1;
              selector += ':nth-of-type(' + index + ')';
            }
          }
          
          return selector;
        }
        
        // 创建元素信息对象
        function createElementInfo(element) {
          const rect = element.getBoundingClientRect();
          return {
            tagName: element.tagName.toLowerCase(),
            id: element.id || '',
            className: element.className || '',
            textContent: element.textContent?.trim().substring(0, 100) || '',
            outerHTML: element.outerHTML.substring(0, 500),
            selector: getElementSelector(element),
            boundingRect: {
              top: rect.top,
              left: rect.left,
              width: rect.width,
              height: rect.height
            }
          };
        }
        
        // 鼠标悬浮事件
        function handleMouseOver(event) {
          if (!isEditMode) return;
          
          event.stopPropagation();
          const element = event.target;
          
          // 跳过body和html元素
          if (element.tagName === 'BODY' || element.tagName === 'HTML') {
            return;
          }
          
          hoverElement = element;
          updateOverlays();
        }
        
        // 鼠标离开事件
        function handleMouseOut(event) {
          if (!isEditMode) return;
          
          event.stopPropagation();
          const element = event.target;
          
          // 只有当鼠标真正离开元素时才清除样式
          if (!element.contains(event.relatedTarget) && element === hoverElement) {
            clearLegacyBorder(element);
            hoverElement = null;
            updateOverlays();
          }
        }
        
        // 点击事件
        function handleClick(event) {
          if (!isEditMode) return;
          
          event.preventDefault();
          event.stopPropagation();
          
          const element = event.target;
          
          // 跳过body和html元素
          if (element.tagName === 'BODY' || element.tagName === 'HTML') {
            return;
          }
          
          selectedElement = element;
          // 选中时如果旧实现已经给该元素上了红/蓝边框，先清掉，避免“双框”
          clearLegacyBorder(element);
          updateOverlays();
          
          // 向父窗口发送选中元素信息
          const elementInfo = createElementInfo(element);
          window.parent.postMessage({
            type: 'ELEMENT_SELECTED',
            data: elementInfo
          }, '*');
        }

        function handleScrollOrResize() {
          if (!isEditMode) return;
          updateOverlays();
        }
        
        // 启用编辑模式
        function enableEditMode() {
          isEditMode = true;
          document.body.style.userSelect = 'none';
          document.body.style.cursor = 'crosshair';
          
          // 添加事件监听器
          document.addEventListener('mouseover', handleMouseOver, true);
          document.addEventListener('mouseout', handleMouseOut, true);
          document.addEventListener('click', handleClick, true);
          window.addEventListener('scroll', handleScrollOrResize, true);
          window.addEventListener('resize', handleScrollOrResize, true);

          updateOverlays();
        }
        
        // 禁用编辑模式
        function disableEditMode() {
          isEditMode = false;
          document.body.style.userSelect = '';
          document.body.style.cursor = '';
          
          // 移除事件监听器
          document.removeEventListener('mouseover', handleMouseOver, true);
          document.removeEventListener('mouseout', handleMouseOut, true);
          document.removeEventListener('click', handleClick, true);
          window.removeEventListener('scroll', handleScrollOrResize, true);
          window.removeEventListener('resize', handleScrollOrResize, true);
          
          // 清除高亮
          clearAllHighlights();
        }
        
        // 清除选择
        function clearSelection() {
          if (selectedElement) {
            clearLegacyBorder(selectedElement);
            selectedElement = null;
            updateOverlays();
            
            window.parent.postMessage({
              type: 'ELEMENT_CLEARED'
            }, '*');
          }
        }
        
        // 监听父窗口消息
        window.addEventListener('message', function(event) {
          const { type } = event.data;
          
          switch (type) {
            case 'ENABLE_EDIT_MODE':
              enableEditMode();
              break;
            case 'DISABLE_EDIT_MODE':
              disableEditMode();
              break;
            case 'CLEAR_SELECTION':
              clearSelection();
              break;
          }
        });
      })();
    `;
  }
}

export default VisualEditor
